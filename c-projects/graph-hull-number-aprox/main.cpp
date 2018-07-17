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
#include <string.h>

#define CHARACTER_INIT_COMMENT '#'

#define DEFAULT_THREAD_PER_BLOCK 256
#define DEFAULT_BLOCK 256 
#define MAX_DEFAULT_SIZE_QUEUE 256

#define SINCLUDED 4
#define PROCESSED 3
#define INCLUDED 2
#define NEIGHBOOR_COUNT_INCLUDED 1
/* */
#define MAX(x, y) (((x) > (y)) ? (x) : (y))
#define MIN(x, y) (((x) < (y)) ? (x) : (y))
#define COPY_ARRAY(SRC,DST,LEN) { memcpy((DST), (SRC), LEN); }

#define verboseSerial false

struct graphCsr {
    int *data;
    //    int nVertices;
    //    int *csrColIdxs;
    //    int *csrRowOffset;
};

int addVertToS(int vert, unsigned char* aux, int *graphData) {
    int countIncluded = 0;
    int nvertices = graphData[0];
    int *csrColIdxs = &graphData[2];
    int *csrRowOffset = &graphData[nvertices + 3];

    if (aux[vert] >= INCLUDED) {
        return countIncluded;
    }
    int headQueue = vert;
    int tailQueue = vert;
    aux[vert] = INCLUDED;

    while (headQueue <= tailQueue) {
        int verti = headQueue;
        if (verti >= nvertices || aux[verti] != INCLUDED) {
            headQueue++;
            continue;
        }
        int end = csrColIdxs[verti + 1];
        for (int j = csrColIdxs[verti]; j < end; j++) {
            int vertn = csrRowOffset[j];
            if (vertn >= nvertices) continue;
            if (vertn != verti && aux[vertn] < INCLUDED) {
                aux[vertn] = aux[vertn] + NEIGHBOOR_COUNT_INCLUDED;
                if (aux[vertn] == INCLUDED) {
                    headQueue = MIN(headQueue, vertn);
                    tailQueue = MAX(tailQueue, vertn);
                }
            }
        }
        aux[verti] = PROCESSED;
        countIncluded++;
    }
    aux[vert] = SINCLUDED;
    return countIncluded;
}

int serialAproxHullNumber(int *graphData) {
    int nvertices = graphData[0];

    unsigned char *aux = new unsigned char [nvertices];
    unsigned char *auxb = new unsigned char [nvertices];
    int minHullSet = nvertices;

    for (int v = 0; v < nvertices; v++) {
        for (int j = 0; j < nvertices; j++) {
            aux[j] = 0;
        }

        int sizeHs = addVertToS(v, aux, graphData);
        int sSize = 1;
        int bv;
        do {
            bv = -1;
            int maiorGrau = 0;
            int maiorDeltaHs = 0;
            for (int i = 0; i < nvertices; i++) {
                if (aux[i] >= INCLUDED) {
                    continue;
                }
                COPY_ARRAY(aux, auxb, nvertices);
                int deltaHsi = addVertToS(i, auxb, graphData);

                int neighborCount = 0;
                for (int j = 0; j < nvertices; j++) {
                    if (auxb[j] == INCLUDED) {
                        neighborCount++;
                    }
                }

                if (bv == -1 || (deltaHsi >= maiorDeltaHs && neighborCount > maiorGrau)) {
                    maiorDeltaHs = deltaHsi;
                    maiorGrau = neighborCount;
                    bv = i;
                }
            }
            sizeHs = sizeHs + addVertToS(bv, aux, graphData);
            sSize++;
        } while (sizeHs < nvertices);
        minHullSet = MIN(minHullSet, sSize);
    }
    free(aux);
    free(auxb);
    return minHullSet;
}

int serialAproxHullNumberGraphs(graphCsr *graphs, int cont) {
    for (int i = 0; i < cont; i++) {
        int minhHullSet = serialAproxHullNumber(graphs[i].data);
        printf("MinHullNumberAprox Graph-%d: %d\n", i, minhHullSet);
    }
}

void processFiles(int argc, char** argv) {
    graphCsr* graphs = (graphCsr*) malloc((sizeof (graphCsr)) * argc);
    std::vector<int> values;

    int contGraph = 0;

    for (int x = 1; x < argc; x++) {
        char* strFile = "graph-test.txt";
        strFile = argv[x];
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
            continue;
        }

        if (strCArray.empty() || strRArray.empty()) {
            perror("Invalid file format");
            continue;
        }

        std::stringstream stream(strCArray.c_str());
        values.clear();

        int n;
        while (stream >> n) {
            values.push_back(n);
        }
        strCArray.clear();

        int numVertices = values.size() - 1;

        //        int *colIdx = new int[numVertices + 1];
        //        std::copy(values.begin(), values.end(), colIdx);
        //        values.clear();
        stream.str("");

        std::stringstream stream2(strRArray);
        while (stream2 >> n) {
            values.push_back(n);
        }
        stream2.str("");
        strRArray.clear();

        //        int sizeRowOffset = values.size();
        //        int *rowOffset = new int[sizeRowOffset];
        //        std::copy(values.begin(), values.end(), rowOffset);


        int numedges = values.size() - (numVertices + 1);
        values.insert(values.begin(), numedges);
        values.insert(values.begin(), numVertices);
        int *data = new int[values.size()];
        std::copy(values.begin(), values.end(), data);

        values.clear();

        graphCsr* graph = &graphs[contGraph];
        graph->data = data;
        //        graph->nVertices = numVertices;
        //        graph->csrColIdxs = colIdx;
        //        graph->csrRowOffset = rowOffset;
        contGraph++;
    }
    serialAproxHullNumberGraphs(graphs, contGraph);
}

void runTest() {
    //    int numVertices = 10;
    //    int colIdx[] = {0, 3, 6, 9, 12, 15, 18, 21, 24, 27, 30};
    //    int sizeRowOffset = numVertices + 1;
    //    int rowOffset[] = {2, 5, 6, 3, 4, 6, 0, 4, 7, 1, 5, 7, 1, 2, 9, 0, 3, 9, 0, 1, 8, 2, 3, 8, 6, 7, 9, 4, 5, 8};
    int data[] = {10, 30,
        0, 3, 6, 9, 12, 15, 18, 21, 24, 27, 30,
        2, 5, 6, 3, 4, 6, 0, 4, 7, 1, 5, 7, 1, 2, 9, 0, 3, 9, 0, 1, 8, 2, 3, 8, 6, 7, 9, 4, 5, 8};
    graphCsr* graph = (graphCsr*) malloc(sizeof (graphCsr));
    graph->data = data;
    //    graph->nVertices = numVertices;
    //    graph->csrColIdxs = colIdx;
    //    graph->csrRowOffset = rowOffset;
    //    int minSerialAprox = serialAproxHullNumber(graph);
    //    printf("MinAproxHullSet: %d\n", minSerialAprox);

    serialAproxHullNumberGraphs(graph, 1);
}

int main(int argc, char** argv) {
    if (argc > 1) {
        processFiles(argc, argv);
    } else {
        runTest();
    }
    return 0;
}