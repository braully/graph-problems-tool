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

#define verboseSerial false

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

void printQueue(int *queue, int headQueue, int tailQueue) {
    printf("Queue(%d):{", tailQueue - headQueue);
    for (int i = headQueue; i <= tailQueue; i++) {
        printf("%2d", queue[i]);
        if (i < tailQueue) {
            printf(", ");
        }
    }
    printf("}\n");
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

int checkConvexityP3(int *csrColIdxs, int nvertices,
        int *csrRowOffset, int sizeRowOffset,
        unsigned char *aux,
        int auxSize,
        int *currentCombination,
        int sizeComb, int idx) {
    //clean aux vector            
    for (int i = 0; i < auxSize; i++) {
        aux[i] = 0;
    }
    int closeCount = 0;
    int maxSizeQueue = MAX((auxSize / 2), MAX_DEFAULT_SIZE_QUEUE);
    int *queue = (int *) malloc(maxSizeQueue * sizeof (int));
    int headQueue = 0;
    int tailQueue = -1;

    for (int i = 0; i < sizeComb; i++) {
        tailQueue = (tailQueue + 1) % maxSizeQueue;
        queue[tailQueue] = currentCombination[i];
    }

    int countExec = 1;

    while (headQueue <= tailQueue) {
        if (verboseSerial) {
            printf("\nP3(k=%2d,c=%d)-%d: ", sizeComb, idx, countExec++);
            printQueue(queue, headQueue, tailQueue);
        }
        int verti = queue[headQueue];
        headQueue = (headQueue + 1) % maxSizeQueue;
        if (verboseSerial) {
            printf("\tv-rm: %d", verti);
        }

        if (aux[verti] < PROCESSED && verti < nvertices) {
            closeCount++;
            int end = csrColIdxs[verti + 1];
            for (int i = csrColIdxs[verti]; i < end; i++) {
                int vertn = csrRowOffset[i];
                if (vertn != verti && vertn < nvertices) {
                    unsigned char previousValue = aux[vertn];
                    if (previousValue < INCLUDED) {
                        aux[vertn] = aux[vertn] + NEIGHBOOR_COUNT_INCLUDED;
                    }
                    if (previousValue < INCLUDED && aux[vertn] >= INCLUDED) {
                        tailQueue = (tailQueue + 1) % maxSizeQueue;
                        queue[tailQueue] = vertn;
                        if (verboseSerial)
                            printf("\t v-inc: %d,", vertn);
                    }
                }
            }
            aux[verti] = PROCESSED;
        }
    }
    free(queue);
    return closeCount;
}

void serialFindHullNumber(int nvertices, int *csrColIdxs, int sizeCsrColIdxs,
        int *csrRowOffset, int sizeRowOffset) {
    int k;
    int currentSize = 0;
    int maxSize = nvertices;
    int sizeCurrentHcp3 = 0;
    int *currentCombination;
    unsigned char *aux = new unsigned char [nvertices];

    bool found = false;

    while (currentSize < maxSize && !found) {
        currentSize++;
        k = currentSize;
        int maxCombination = maxCombinations(nvertices, k);
        currentCombination = (int *) malloc(k * sizeof (int));
        initialCombination(nvertices, k, currentCombination);

        for (int i = 0; i < maxCombination && !found; i++) {
            sizeCurrentHcp3 = checkConvexityP3(csrColIdxs, nvertices,
                    csrRowOffset, sizeRowOffset, aux, nvertices, currentCombination, k, i);
            found = (sizeCurrentHcp3 == nvertices);
            if (!found)
                nextCombination(nvertices, k, currentCombination);
        }
        if (found) {
            printf("\nResult Serial\n");
            printCombination(currentCombination, currentSize);
            printf("\nHull number=%d\n|S| = %d\n|hcp3(S)| = %d\n|V(g)| = %d\n", k, k, sizeCurrentHcp3, nvertices);
        }
        free(currentCombination);
    }
    free(aux);
}

int main(int argc, char** argv) {
    int opt = 0;
    char* strFile = "graph-test.txt";
    //    char* strFile = "graph-test/graph-csr-41289295013299317.txt";
    bool serial = true;
    bool binary = true;

    if ((argc > 1)) {
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
    serialFindHullNumber(numVertices, colIdx, sizeRowOffset, rowOffset, sizeRowOffset);
    return 0;
}