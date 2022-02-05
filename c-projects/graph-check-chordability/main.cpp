#include <iostream>
#include <set>
#include <vector>
#include <map>
#include <algorithm>
#include <stdio.h>
#include <stdlib.h>

// Structures

// ord(i) has the number of vertex i in the ordenation
// vert(i) has the vertex of number i in the ordenation
typedef struct order_alpha {
	std::vector<int> ord;
	std::vector<int> vert;
} order;


//Global variables
int *mat;
int N_vertices;


// Declarations
bool edge(int i, int j);
bool cmp_second(std::pair<int, int> a, std::pair<int, int> b);
int find(std::vector<int>& parent, int i);
void unite(std::vector<int>& parent, int x, int y);
std::vector<std::set<int>> connected_components(std::set<int> vertices);
order max_card_search(std::set<int> X_set);
bool zero_fill_in(order ord, std::set<int> X_set);

//Definitions

// Creates an adjacency matrix from file
void graphAdjMatrixFromFile(char *fileName) {
    if (fileName == NULL) {
        return;
    }

    FILE * filePointer;
    char line[1000];
    filePointer = fopen(fileName, "r");
    fgets(line, 1000, filePointer);

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

	N_vertices = numberOfVertice;
	mat = (int *) malloc(N_vertices * N_vertices * sizeof (int));
	
    int j = 0;
    rewind(filePointer);
    for (i = 0; i < N_vertices; i++) {
        for (j = 0; j < N_vertices; j++) {
            char c = NULL;
            while (c == ' ' || c == NULL || c == '\n') {
                fscanf(filePointer, "%c", &c);
            }
            *(mat + i*N_vertices + j) = c - '0';
        }
    }

    fclose(filePointer);
}

// Returns true if there is an edge connecting i and j
// Returns false otherwise
bool edge(int i, int j) {
	return *(mat + i*N_vertices + j);
}

// Function to sort a vector of pairs by the second value
bool cmp_second(std::pair<int, int> a, std::pair<int, int> b) {
	return a.second < b.second;
}

// Implementation of union-find algorithm

// Find function:
int find(std::vector <int>& parent, int i) {
	if (parent[i] == i)
		return i;
	return find(parent, parent[i]);
}

// Union function:
void unite(std::vector <int>& parent, int x, int y) {
	int xset = find(parent, x);
	int yset = find(parent, y);
	parent[yset] = xset;
}

// Returns a vector of sets
// Each set has all the vertices in the same component
// There is no path connecting vertices from different sets
// using only the vertices in "vertices" set
std::vector<std::set<int>> connected_components(std::set<int> vertices) {
	int size = vertices.size();
	std::vector <int> parent;
	std::vector <std::set<int>> components;
	std::vector <std::set<int>> result;
	parent.resize(size);
	components.resize(size);

	// Using union-find to identify the components
	for (int i = 0; i < size; i++)
		parent[i] = i;

	for (auto i = vertices.begin(); i != vertices.end(); i++) {
		for (auto j = i; j != vertices.end(); j++) {
			if (edge(*i, *j)) {
				unite(parent, distance(vertices.begin(), i), distance(vertices.begin(), j));
			}
		}
	}

	// Making a vector of sets with the vertices in each component
	for (auto i = vertices.begin(); i != vertices.end(); i++) {
		components[find(parent, distance(vertices.begin(), i))].insert(*i);
	}

	// Excluding empty sets
	for (auto i = components.begin(); i != components.end(); i++) {
		if (!(*i).empty())
			result.push_back(*i);
	}

	return result;
}

// Orders the vertices of a graph in X_set by maximum cardinality search
order max_card_search(std::set<int> X_set) {
	std::vector<int> X(X_set.begin(), X_set.end());
	int N = X.size();

	// We use a vector of sets, each one having
	// the vertices with i numbered neighboors
	// sets[i].count(j) = 1 if vertex j is in set i
	// sets[i].count(j) = 0 if it is not
	std::vector<std::set<int>> sets;
	sets.resize(N);

	// size[i] is the number of numbered neighbors to the vertex in position i
	std::vector <int> size;
	size.resize(N);

	// ord has the ordenation
	order ord;
	ord.ord.resize(N);
	ord.vert.resize(N);

	// All vertices have 0 numbered neighbors in the beginning
	for (int i = 0; i < N; i++)
		sets[0].insert(X[i]);

	// j is the biggest with set not empty
	int j = 0;

	// For each vertex 
	for (int i = N - 1; i >= 0; i--) {

		// Gets any vertex with the biggest number of numbered neighbors
		while (sets[j].empty())
			j--;
		int v = *(sets[j].begin());
		sets[j].erase(v);

		// Vertex v receives number i
		int v_index = distance(X.begin(), find(X.begin(), X.end(), v));
		ord.ord[v_index] = i;
		ord.vert[i] = v;
		size[v_index] = -1;

		// For each edge (v, w) such that w is unumbered
		// adds 1 to the number of numbered vertices of w
		// and places it in the next set
		for (int w = 0; w < N; w++) {
			if (edge(v, X[w]) && size[w] >= 0) {
				sets[size[w]].erase(X[w]);
				size[w]++;
				sets[size[w]].insert(X[w]);
			}
		}

		// Each time a vertex receives a number, the maximum number
		// of numbered neighbors of a vertex is <= j+1
		j++;
	}
	return ord;
}

// Checks if the fill in based on a vertex ordenation of the graph
// is empty (so the graph is chordal) or not
// Considers only vertices in X_set
// Returns true to empty fill in
// Returns false otherwise
bool zero_fill_in(order ord, std::set<int> X_set) {
	std::vector<int> X(X_set.begin(), X_set.end());
	int N = X.size();
	// f[v] is the follower of v, i.e. the neighbor of v
	// with the smallest ordering that is bigger than v's
	std::vector<int> f;
	f.resize(N);

	// index[v] is the biggest vertex between v and
	// v's already processed neighbors 
	std::vector<int> index;
	index.resize(N);

	// We begin processing the vertex with the smallest ordering (i)
	for (int i = 0; i < N; i++) {
		int w = ord.vert[i];
		int w_index = distance(X.begin(), find(X.begin(), X.end(), w));
		f[w_index] = w;
		index[w_index] = i;

		// For each neighbor v of w with ordering smaller than w
		// that it's already processed neighbors have ordering
		// smaller than w too: connect w to v and to it's followers
		// that respect the same properties
		for (int v = 0; v < N; v++) {
			if (edge(w, X[v]) && ord.ord[v] < i) {
				int x = X[v];
				int x_index = distance(X.begin(), find(X.begin(), X.end(), x));
				while (index[x_index] < i) {
					// w is an already processed neighbor of x with
					// bigger ordering, so we update index[x]
					index[x_index] = i;

					// We would have to connect x to w in the fill in,
					// so if it isn't connected, the fill in is not empty
					// and so we return false
					if (!edge(w, x))
						return false;

					// We repeat with the follower of x
					x = f[x_index];
					x_index = distance(X.begin(), find(X.begin(), X.end(), x));
				}

				// If the last follower of v has itself as a follower,
				// it shall follow w
				if (f[x_index] == x)
					f[x_index] = w;
			}
		}
	}

	// If we made it here, the fill in is empty
	return true;
}

// Input the number of vertices and adjacency matrix
// The graph must be connected
// Prints if graph is chordal or not
int main(int argc, char** argv) {
	if (argc > 1){
		graphAdjMatrixFromFile(argv[1]);
		
		std::set<int> X;
		for (int i = 0; i < N_vertices; i++)
			X.insert(i);

		std::vector<std::set<int>> components = connected_components(X);

		for (auto& component : components) {
			if (!zero_fill_in(max_card_search(component), component)) {
				std::cout << "The graph is not chordal\n";
				free(mat);
				return 0;
			}
		}

		std::cout << "The graph is chordal\n";
		free(mat);
	}
	return 0;
}