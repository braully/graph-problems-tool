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
#include <vector>
#include <math.h> 

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
int result_d;
volatile bool found = false;

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

void initialCombination(int n, int k, int* combinationArray) {
    for (int i = 0; i < k; i++) {
        combinationArray[i] = i;
    }
}

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

void serialFindCaratheodoryNumber(int nvertices, int *csrColIdxs, int sizeCsrColIdxs,
        int *csrRowOffset, int sizeRowOffset, int maxCombination, int k, int offset) {
    int idx = 0;
    if (idx == 0) {
        result_d = 0;
    }

    void *memspace = (void *) malloc(sizeof (int)*k + sizeof (int)*nvertices + sizeof (unsigned char)*nvertices);

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
    int execCount = 0;
    while (k_i < limmit && !result_d) {
        execCount++;
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
            printf("\nCartheodory Find - Thread-%d: sizederivated:%d k:%d k_i:%d",
                    idx, sizederivated, k, k_i);
        }
    }
    free(memspace);
}

int findCaratheodoryNumber(int nvertices, int *csrColIdxs, int sizeCsrColIdxs,
        int *csrRowOffset, int sizeRowOffset, int maxCombination, int k, int offset) {
    int idx = 0;
    int result_d = 0;

    void *memspace = (void *) malloc(sizeof (int)*k + sizeof (int)*nvertices + sizeof (unsigned char)*nvertices);

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

    while (k_i < limmit && !result_d) {
        for (int i = 0; i < nvertices; i++) {
            aux[i] = 0;
            auxc[i] = 0;
        }

        int headQueue = nvertices;
        int tailQueue = -1;

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
            printf("\nCartheodory Find - sizederivated:%d k:%d k_i:%d",
                    sizederivated, k, k_i);
            printf("\nResult Serial Binary");
            printf("\n∂H(S) = {");
            for (int i = 0; i < nvertices; i++)
                if (aux[i] >= INCLUDED) printf("%d,", i);
            printf("}\n");
        }
    }
    free(memspace);
    return result_d;
}

void findSerialCaratheodoryNumberBinaryStrategy(int verticesCount, int* csrColIdxs,
        int csrColIdxsSize, int* csrRowOffset, int sizeRowOffset) {
    int maxSizeSet = (verticesCount + 1) / 2;
    int k = 0;
    int left = 0;
    int rigth = maxSizeSet;
    int lastSize = -1;
    int lastIdxCaratheodorySet = -1;
    int result_h = 0;

    while (left <= rigth) {
        k = (left + rigth) / 2;
        int maxCombination = maxCombinations(verticesCount, k);
        double ceilval = ceil(maxCombination / 3.0);
        int threadsPerBlock = MIN(ceilval, DEFAULT_THREAD_PER_BLOCK);
        int blocksPerGrid = ceil(maxCombination / (double) (threadsPerBlock) / 7);
        int offset = ceil(maxCombination / (double) (blocksPerGrid * threadsPerBlock));
        //        http://stackoverflow.com/questions/34041372/access-cuda-global-device-variable-from-host
        //        http://stackoverflow.com/questions/12505750/how-can-a-global-function-return-a-value-or-break-out-like-c-c-does

        result_h = 0;
        if (k > 0) {
            printf("\nkernelFindCaratheodoryNumber: szoffset=%d nvs=%d k=%d max=%d blks=%d tdsPerBlock=%d offset=%d",
                    sizeRowOffset, verticesCount, k, maxCombination, blocksPerGrid, threadsPerBlock, offset);
            result_h = findCaratheodoryNumber(verticesCount, csrColIdxs, csrColIdxsSize, csrRowOffset,
                    sizeRowOffset, maxCombination, k, maxCombination);
        }

        if (result_h > 0) {
            lastSize = k;
            lastIdxCaratheodorySet = result_h - 1;
            left = k + 1;
            result_h = 0;
            //            printf("\nCaratheodory set size=%d..Ok idx=%d", k, lastIdxCaratheodorySet);
        } else {
            rigth = k - 1;
            //            printf("\nCaratheodory set size=%d..Not", k);
        }
    }

    if (lastSize > 0) {
        int *currentCombination = (int *) malloc(lastSize * sizeof (int));
        initialCombination(verticesCount, lastSize, currentCombination, lastIdxCaratheodorySet);
        printCombination(currentCombination, lastSize);
        printf("\nS=%d-Comb(%d,%d) \nCaratheodroy number(c) = %d\n",
                lastIdxCaratheodorySet, verticesCount, lastSize, lastSize);
    }
    //    else {
    //        printf("\nCaratheodory set not found!");
    //    }
}

int main(int argc, char** argv) {
    int opt = 0;
    char* strFile = "graph-test.txt";
    //    char* strFile = "graph-test/graph-csr-41289295013299317.txt";

    if (argc > 1) {
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
        //printf("file '%s' not found!", filepath.c_str());
        return -1;
    }

    if (strCArray.empty() || strRArray.empty()) {
        perror("Invalid file format");
        return -1;
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
    findSerialCaratheodoryNumberBinaryStrategy(numVertices, colIdx, sizeRowOffset, rowOffset, sizeRowOffset);
    return 0;
}

