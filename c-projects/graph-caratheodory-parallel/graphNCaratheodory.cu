#include <stdio.h>
#include <stdlib.h>
#include <iostream>
#include <string>
#include <sstream>
#include <dirent.h>
#include <unistd.h>
#include <fstream>
#include <sys/stat.h>
#include <sys/types.h>
#include <cuda_runtime.h>
#include <cuda.h>
#include <vector>

#define CHARACTER_INIT_COMMENT '#'

#define DEFAULT_THREAD_PER_BLOCK 256
#define DEFAULT_BLOCK 256 
#define MAX_DEFAULT_SIZE_QUEUE 256
#define PROCESSED 3
#define INCLUDED 2
#define NEIGHBOOR_COUNT_INCLUDED 1
/* */
#define MAX(x, y) (((x) > (y)) ? (x) : (y))
#define MIN(x, y) (((x) < (y)) ? (x) : (y))

//extern __device__ int result[2];
__device__ int result_d;
__device__ volatile bool found = false;

__host__ __device__
int maxCombinations(int n, int k) {
    if (n == 0 || k == 0) {
        return 0;
    }
    if (n < k) {
        return 0;
    }
    if (n == k) {
        return 1;
    }
    int delta, idxMax;
    if (k < n - k) {
        delta = n - k;
        idxMax = k;
    } else {
        delta = k;
        idxMax = n - k;
    }

    int ans = delta + 1;
    for (int i = 2; i <= idxMax; ++i) {
        ans = (ans * (delta + i)) / i;
    }
    return ans;
}

__host__ __device__
void initialCombination(int n, int k, int* combinationArray, int idx) {
    int a = n;
    int b = k;
    int x = (maxCombinations(n, k) - 1) - idx;
    for (int i = 0; i < k; ++i) {
        combinationArray[i] = a - 1;
        while (maxCombinations(combinationArray[i], b) > x) {
            --combinationArray[i];
        }
        x = x - maxCombinations(combinationArray[i], b);
        a = combinationArray[i];
        b = b - 1;
    }

    for (int i = 0; i < k; ++i) {
        combinationArray[i] = (n - 1) - combinationArray[i];
    }
}

__host__ __device__
void initialCombination(int n, int k, int* combinationArray) {
    for (int i = 0; i < k; i++) {
        combinationArray[i] = i;
    }
}

__host__ __device__
void nextCombination(int n,
        int k,
        int* currentCombination) {
    if (currentCombination[0] == n - k) {
        return;
    }
    int i;
    for (i = k - 1; i > 0 && currentCombination[i] == n - k + i; --i);
    ++currentCombination[i];
    for (int j = i; j < k - 1; ++j) {
        currentCombination[j + 1] = currentCombination[j] + 1;
    }
}

__host__ __device__
void printCombination(int *currentCombination,
        int sizeComb) {
    printf("S = {");
    for (int i = 0; i < sizeComb; i++) {
        printf("%2d", currentCombination[i]);
        if (i < sizeComb - 1) {
            printf(", ");
        }
    }
    printf(" }");
}

__global__
void kernelFindCaratheodoryNumber(int nvertices, int *csrColIdxs, int sizeCsrColIdxs,
        int *csrRowOffset, int sizeRowOffset, int maxCombination, int k, int offset) {
    int idx = blockIdx.x * blockDim.x + threadIdx.x;
    if (idx == 0) {
        result_d = 0;
        found = false;
    }
    __syncthreads();

    //if (1)
    //    return;

    void *memspace = (void *) malloc(sizeof (int)*k + sizeof (int)*nvertices + sizeof (unsigned char)*nvertices);

    if (memspace == NULL) {
        printf("failed memory allocation\n");
        return;
    }

    int *currentCombinations = (int *) memspace;
    int* aux = (int *) (memspace + sizeof (int)*k);
    unsigned char *auxc = (unsigned char *) (memspace + sizeof (int)*k + sizeof (int)*nvertices);

    int sizederivated = 0;
    int limmit = (idx + 1) * offset;
    int k_i = idx * offset;

    if (limmit > maxCombination) {
        limmit = maxCombination;
    }

    initialCombination(nvertices, k, currentCombinations, k_i);

    while (k_i < limmit && !found) {
        //clean aux vector            
        for (int i = 0; i < nvertices; i++) {
            aux[i] = 0;
            auxc[i] = 0;
        }

        int headQueue = nvertices;
        int tailQueue = 0;

        for (int i = 0; i < k; i++) {
            int idi = currentCombinations[i];
            aux[idi] = INCLUDED;
            auxc[idi] = 1;
            headQueue = MIN(headQueue, idi);
            tailQueue = MAX(tailQueue, idi);
        }

        while (headQueue <= tailQueue) {
            int verti = headQueue;
            if (verti >= nvertices || aux[verti] != INCLUDED) {
                headQueue++;
                continue;
            }

            int end = csrColIdxs[verti + 1];
            for (int i = csrColIdxs[verti]; i < end; i++) {
                int vertn = csrRowOffset[i];
                if (vertn >= nvertices) continue;
                if (vertn != verti && aux[vertn] < INCLUDED) {
                    aux[vertn] = aux[vertn] + NEIGHBOOR_COUNT_INCLUDED;
                    if (aux[vertn] == INCLUDED) {
                        headQueue = MIN(headQueue, vertn);
                        tailQueue = MAX(tailQueue, vertn);
                    }
                    auxc[vertn] = auxc[vertn] + auxc[verti];
                }
            }
            aux[verti] = PROCESSED;
        }

        bool checkDerivated = false;

        for (int i = 0; i < nvertices; i++)
            if (auxc[i] >= k && aux[i] == PROCESSED) {
                checkDerivated = true;
                break;
            }

        if (checkDerivated) {
            for (int i = 0; i < k; i++) {
                int p = currentCombinations[i];
                headQueue = nvertices;
                tailQueue = -1;

                for (int j = 0; j < nvertices; j++) {
                    auxc[j] = 0;
                }

                for (int j = 0; j < k; j++) {
                    int v = currentCombinations[j];
                    if (v != p) {
                        auxc[v] = INCLUDED;
                        headQueue = MIN(headQueue, v);
                        tailQueue = MAX(tailQueue, v);
                    }
                }
                while (headQueue <= tailQueue) {
                    int verti = headQueue;

                    if (verti >= nvertices || auxc[verti] != INCLUDED) {
                        headQueue++;
                        continue;
                    }
                    aux[verti] = 0;
                    int end = csrColIdxs[verti + 1];
                    for (int x = csrColIdxs[verti]; x < end; x++) {
                        int vertn = csrRowOffset[x];
                        if (vertn != verti && auxc[vertn] < INCLUDED) {
                            auxc[vertn] = auxc[vertn] + NEIGHBOOR_COUNT_INCLUDED;
                            if (auxc[vertn] == INCLUDED) {
                                headQueue = MIN(headQueue, vertn);
                                tailQueue = MAX(tailQueue, vertn);
                            }
                        }
                    }
                    auxc[verti] = PROCESSED;
                }
            }
            sizederivated = 0;
            for (int i = 0; i < nvertices; i++)
                if (aux[i] >= INCLUDED) sizederivated++;
        }

        if (sizederivated == 0) {
            nextCombination(nvertices, k, currentCombinations);
            k_i++;
        } else {
            result_d = k_i + 1;
            found = true;
            printf("\nCartheodory Find - Thread-%d: sizederivated=%d k=%d k_i=%d",
                    idx, sizederivated, k, k_i);
        }
    }

    //    printf("\nThread-%d: szoffset=%d nvs=%d k=%d k_i=[%d, %d]=%d",
    //            idx, sizeRowOffset, nvertices, k, idx * offset, limmit, execCount);
    free(memspace);
}

bool checkIfCaratheodorySet() {
    for (int i = 0; i < nvertices; i++) {
        aux[i] = 0;
        auxc[i] = 0;
    }

    int headQueue = nvertices;
    int tailQueue = 0;

    for (int i = 0; i < k; i++) {
        int idi = currentCombinations[i];
        aux[idi] = INCLUDED;
        auxc[idi] = 1;
        headQueue = MIN(headQueue, idi);
        tailQueue = MAX(tailQueue, idi);
    }

    while (headQueue <= tailQueue) {
        int verti = headQueue;
        if (verti >= nvertices || aux[verti] != INCLUDED) {
            headQueue++;
            continue;
        }

        int end = csrColIdxs[verti + 1];
        for (int i = csrColIdxs[verti]; i < end; i++) {
            int vertn = csrRowOffset[i];
            if (vertn >= nvertices) continue;
            if (vertn != verti && aux[vertn] < INCLUDED) {
                aux[vertn] = aux[vertn] + NEIGHBOOR_COUNT_INCLUDED;
                if (aux[vertn] == INCLUDED) {
                    headQueue = MIN(headQueue, vertn);
                    tailQueue = MAX(tailQueue, vertn);
                }
                auxc[vertn] = auxc[vertn] + auxc[verti];
            }
        }
        aux[verti] = PROCESSED;
    }

    bool checkDerivated = false;
    bool isCaratheodory = false;

    for (int i = 0; i < nvertices; i++)
        if (auxc[i] >= k && aux[i] == PROCESSED) {
            checkDerivated = true;
            break;
        }

    if (checkDerivated) {
        for (int i = 0; i < k; i++) {
            int p = currentCombinations[i];
            headQueue = nvertices;
            tailQueue = -1;

            for (int j = 0; j < nvertices; j++) {
                auxc[j] = 0;
            }

            for (int j = 0; j < k; j++) {
                int v = currentCombinations[j];
                if (v != p) {
                    auxc[v] = INCLUDED;
                    headQueue = MIN(headQueue, v);
                    tailQueue = MAX(tailQueue, v);
                }
            }
            while (headQueue <= tailQueue) {
                int verti = headQueue;

                if (verti >= nvertices || auxc[verti] != INCLUDED) {
                    headQueue++;
                    continue;
                }
                aux[verti] = 0;
                int end = csrColIdxs[verti + 1];
                for (int x = csrColIdxs[verti]; x < end; x++) {
                    int vertn = csrRowOffset[x];
                    if (vertn != verti && auxc[vertn] < INCLUDED) {
                        auxc[vertn] = auxc[vertn] + NEIGHBOOR_COUNT_INCLUDED;
                        if (auxc[vertn] == INCLUDED) {
                            headQueue = MIN(headQueue, vertn);
                            tailQueue = MAX(tailQueue, vertn);
                        }
                    }
                }
                auxc[verti] = PROCESSED;
            }
        }
        sizederivated = 0;
        for (int i = 0; i < nvertices; i++)
            if (aux[i] >= INCLUDED) sizederivated++;
    }

    if (sizederivated == 0) {
        nextCombination(nvertices, k, currentCombinations);
        k_i++;
    } else {
        result_d = k_i + 1;
        found = true;
        printf("\nCartheodory Find - Thread-%d: sizederivated=%d k=%d k_i=%d",
                idx, sizederivated, k, k_i);
    }
    return isCaratheodory;
}

void findParallelCaratheodoryNumberBinaryStrategy(int verticesCount, int* csrColIdxs,
        int csrColIdxsSize, int* csrRowOffset, int sizeRowOffset) {
    int* csrColIdxsGpu;
    int* csrRowOffsetGpu;

    int numBytesClsIdx = sizeof (int)*csrColIdxsSize;
    cudaMalloc((void**) &csrColIdxsGpu, numBytesClsIdx);

    int numBytesRowOff = sizeof (int)*sizeRowOffset;
    cudaMalloc((void**) &csrRowOffsetGpu, numBytesRowOff);

    cudaError_t r = cudaMemcpy(csrColIdxsGpu, csrColIdxs, numBytesClsIdx, cudaMemcpyHostToDevice);
    if (r != cudaSuccess) {
        fprintf(stderr, "Failed to copy memory 1 \nError: %s\n", cudaGetErrorString(r));
        exit(EXIT_FAILURE);
    }
    r = cudaMemcpy(csrRowOffsetGpu, csrRowOffset, numBytesRowOff, cudaMemcpyHostToDevice);
    if (r != cudaSuccess) {
        fprintf(stderr, "Failed to copy memory 2 \nError: %s\n", cudaGetErrorString(r));
        exit(EXIT_FAILURE);
    }

    int maxSizeSet = (verticesCount + 1) / 2;
    int k = 0;
    int left = 0;
    int rigth = maxSizeSet;
    int lastSize = -1;
    int lastIdxCaratheodorySet = -1;

    r = cudaGetLastError();

    int result_h = 0;

    while (left <= rigth) {
        k = (left + rigth) / 2;
        int maxCombination = maxCombinations(verticesCount, k);
        int threadsPerBlock = MIN(ceil(maxCombination / 3.0), DEFAULT_THREAD_PER_BLOCK);
        int blocksPerGrid = MIN(ceil(maxCombination / (double) (threadsPerBlock) / 7), DEFAULT_BLOCK);
        int offset = ceil(maxCombination / (double) (blocksPerGrid * threadsPerBlock));
        //        http://stackoverflow.com/questions/34041372/access-cuda-global-device-variable-from-host

        if (k > 0) {
            printf("\nkernelFindCaratheodoryNumber: szoffset=%d nvs=%d k=%d max=%d blks=%d tdsPerBlock=%d offset=%d",
                    sizeRowOffset, verticesCount, k, maxCombination, blocksPerGrid, threadsPerBlock, offset);
            kernelFindCaratheodoryNumber << <blocksPerGrid, threadsPerBlock>>>(verticesCount, csrColIdxsGpu, csrColIdxsSize, csrRowOffsetGpu,
                    sizeRowOffset, maxCombination, k, offset);
        }

        r = cudaDeviceSynchronize();
        if (r != cudaSuccess) {
            fprintf(stderr, "Failed cudaDeviceSynchronize \nError: %s\n", cudaGetErrorString(r));
            exit(EXIT_FAILURE);
        }

        r = cudaMemcpyFromSymbol(&result_h, result_d, sizeof (int));
        if (r != cudaSuccess) {
            fprintf(stderr, "Failed to copy memory 4 \nError: %s\n", cudaGetErrorString(r));
            exit(EXIT_FAILURE);
        }

        r = cudaGetLastError();
        if (r != cudaSuccess) {
            fprintf(stderr, "Failed in kernelFindCaratheodoryNumber \nError: %s\n", cudaGetErrorString(r));
            exit(EXIT_FAILURE);
        }

        if (result_h > 0) {
            lastSize = k;
            lastIdxCaratheodorySet = result_h - 1;
            left = k + 1;
            result_h = 0;
            printf("\nCaratheodory set size=%d..Ok idx=%d", k, lastIdxCaratheodorySet);
        } else {
            rigth = k - 1;
            printf("\nCaratheodory set size=%d..Not", k);
        }
    }

    if (lastSize > 0) {
        printf("\nResult Parallel Binary\n");
        int *currentCombination = (int *) malloc(lastSize * sizeof (int));
        initialCombination(verticesCount, lastSize, currentCombination, lastIdxCaratheodorySet);
        printCombination(currentCombination, lastSize);
        printf("\nS=%d-Comb(%d,%d) \n|S| = %d\n",
                lastIdxCaratheodorySet, verticesCount, lastSize, lastSize);
    } else {
        printf("\nCaratheodory set not found!");
    }
    cudaFree(csrRowOffsetGpu);
    cudaFree(csrColIdxsGpu);
}

int main(int argc, char** argv) {
    char* strFile = "graph-test/graph-csr-8775879977288244551.txt";
    bool parallel = false;
    bool binary = false;

    if ((argc <= 1) || (argv[argc - 1] == NULL) || (argv[argc - 1][0] == '-')) {
        parallel = true;
        binary = true;
    } else {
        strFile = argv[1];
    }

    DIR *dpdf;
    struct dirent *epdf;
    struct stat filestat;

    dpdf = opendir(strFile);
    std::string filepath = std::string(strFile);
    while (dpdf && (epdf = readdir(dpdf))) {
        filepath = std::string(strFile) + "/" + epdf->d_name;
        if (epdf->d_name == "." || epdf->d_name == "..")
            continue;
        if (stat(filepath.c_str(), &filestat))
            continue;
        if (S_ISDIR(filestat.st_mode))
            continue;
        else break;
    }
    closedir(dpdf);

    std::string line, strCArray, strRArray;
    std::ifstream infile(filepath.c_str());

    if (infile) {
        while (getline(infile, line)) {
            if (line.at(0) != CHARACTER_INIT_COMMENT) {
                if (strCArray.empty()) {
                    strCArray = line;
                } else if (strRArray.empty()) {
                    strRArray = line;
                }
            }
        }
        infile.close();
    } else {
        printf("file '%s' not found!", filepath.c_str());
        return;
    }

    if (strCArray.empty() || strRArray.empty()) {
        perror("Invalid file format");
        return;
    }

    std::stringstream stream(strCArray.c_str());
    std::vector<int> values;

    int n;
    while (stream >> n) {
        values.push_back(n);
    }
    strCArray.clear();

    int numVertices = values.size() - 1;
    int *colIdx = new int[numVertices + 1];
    std::copy(values.begin(), values.end(), colIdx);
    values.clear();
    stream.str("");

    std::stringstream stream2(strRArray);
    while (stream2 >> n) {
        values.push_back(n);
    }
    stream2.str("");
    strRArray.clear();

    int sizeRowOffset = values.size();
    int *rowOffset = new int[sizeRowOffset];
    std::copy(values.begin(), values.end(), rowOffset);
    values.clear();

    printf("\nProcess file: %s", filepath.c_str());
    findParallelCaratheodoryNumberBinaryStrategy(numVertices, colIdx, sizeRowOffset, rowOffset, sizeRowOffset);
    return 0;
}

