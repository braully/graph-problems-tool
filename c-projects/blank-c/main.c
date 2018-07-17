#include <stdio.h>
#include <stdlib.h>

void graphAdjMatrixFromFile(char *fileName) {
    if (fileName == NULL) {
        return;
    }

    FILE * filePointer;
    char line[1000];
    filePointer = fopen(fileName, "r");
    fgets(line, 1000, filePointer);

    printf("\nfile: %s", fileName);
    printf("\nline: %s", line);

    int i = 0;
    int numberOfVertice = 0;
    int lastIndex = -1;

    while (line[i] != NULL) {
        if (line[i] == ' ' && i > lastIndex + 1) {
            numberOfVertice++;
            lastIndex = i;
        }
        i++;
    }
    if (i > 0) {
        numberOfVertice++;
    }

    printf("Nº de Vertices: %d\n", numberOfVertice);

    int *adj[numberOfVertice];
    int j = 0;

    rewind(filePointer);
    for (i = 0; i < numberOfVertice; i++) {
        adj[i] = (int *) malloc(numberOfVertice * sizeof (int));
        for (j = 0; j < numberOfVertice; j++) {
            char c = NULL;
            while (c == ' ' || c == NULL || c == '\n') {
                fscanf(filePointer, "%c", &c);
            }
            adj[i][j] = c - '0';
        }
    }


    printf("Graph readed\n");
    for (i = 0; i < numberOfVertice; i++) {
        for (j = 0; j < numberOfVertice; j++) {
            printf("%d ", adj[i][j]);
        }
        printf("\n");
    }

    fclose(filePointer);
}

int main(int argc, char** argv) {
    printf("Blank c application\n");

    if (argc > 1) {
        graphAdjMatrixFromFile(argv[1]);
    }
    return (EXIT_SUCCESS);
}

