#include <iostream>
#include <set>
#include <vector>
#include <map>
#include <algorithm>
#include <stdlib.h>
#include <stdio.h>


//Global variables
int *mat;
int N_vertices;

// Declarations
void graphAdjMatrixFromFile(char *fileName);
int find(std::vector<int>& parent, int i);
void unite(std::vector<int>& parent, int x, int y);
bool edge(int i, int j);
std::set<int> bigst_set(std::set<int> A, std::set<int> B);
std::set<int> set_U(std::set<int> A, std::set<int> B);
std::set<int> set_M(std::set<int> A, std::set<int> B);
std::set<int> set_I(std::set<int> A, std::set<int> B);
bool set_S(std::set<int> A, std::set<int> B);
std::vector<std::set<int>> connected_components(std::set<int> vertices);
std::map<int, int> degrees(std::set<int> X);
bool cmp_second(std::pair<int, int> a, std::pair<int, int> b);
std::set<int> N(int v, std::set<int> X);
std::set<int> N_C(int v, std::set<int> X);
std::set<int> N2(int v, std::set<int> X);
bool dominates(int v, int u, std::set<int> X);
std::set<int> MIS1(std::set<int> X, std::set<int> S);
std::set<int> MIS2(std::set<int> X, std::set<int> S);
std::set<int> MIS(std::set<int> X);


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

// Implementation of union-find algorithm

// Find function:
int find(std::vector <int> &parent, int i){
	if (parent[i] == i)
		return i;
	return find(parent, parent[i]);
}

// Union function:
void unite(std::vector <int> &parent, int x, int y){
	int xset = find(parent, x);
	int yset = find(parent, y);
	parent[yset] = xset;
}

// Returns true if there is an edge connecting i and j
// Returns false otherwise
bool edge(int i, int j) {
	return *(mat + i*N_vertices + j);
}

// Returns the biggest set
// Chooses the first in case of draw
std::set<int> bigst_set(std::set<int> A, std::set<int> B) {
	if (A.size() < B.size())
		return B;
	else
		return A;
}

// Returns the union of two sets A U B
std::set<int> set_U(std::set<int> A, std::set<int> B) {
	std::set<int> result = A;
	result.insert(B.begin(), B.end());
	
	return result;
}

// Returns the difference of two sets A\B
std::set<int> set_M(std::set<int> A, std::set<int> B) {
	std::set<int> result = A;
	for (auto& it : B) {
		if (A.count(it))
			result.erase(it);
	}

	return result;
}

// Returns the intersection of two sets A and B
std::set<int> set_I(std::set<int> A, std::set<int> B) {
	std::set<int> result;

	for (auto& it : A) {
		if (B.count(it))
			result.insert(it);
	}

	return result;
}

// Returns true if A is subset of B
// Returns false otherwise
bool set_S(std::set<int> A, std::set<int> B) {
	for (auto& it : A)
		if (!B.count(it))
			return false;
	
	return true;
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

// Returns a map with {key : value} = {vertex : degree}
// considering only vertices in X
std::map<int, int> degrees(std::set<int> X) {
	std::map<int, int> d;
	
	for (auto i = X.begin(); i != X.end(); i++)
		d.insert({ *i, 0 });

	for (auto i = X.begin(); i != X.end(); i++) {
		for (auto j = i; j != X.end(); j++) {
			if (edge(*i, *j)) {
				d[*i]++;
				d[*j]++;
			}
		}
	}

	return d;
}

// Function to sort a vector of pairs by the second value
bool cmp_second(std::pair<int, int> a, std::pair<int, int> b){
	return a.second < b.second;
}

// Returns the set of neighboors of v that are in X
std::set<int> N(int v, std::set<int> X) {
	std::set<int> result;
	for (auto& it : X) {
		if (edge(v, it))
			result.insert(it);
	}

	return result;
}

// Returns the set of neighboors of v that are in X, including v
std::set<int> N_C(int v, std::set<int> X) {
	return set_U(N(v, X), {v});
}

// Returns the neighboors of the neighboors of v
// excluding the neighboors of v and v
std::set<int> N2(int v, std::set<int> X) {
	std::set<int> result;
	std::set<int> neighboors = N(v, X);

	for (auto& it : neighboors) 
		result = set_U(result, N(it, X));

	result = set_M(result, neighboors);
	result.erase(v);

	return result;
}

// Returns true if vertex v dominates vertex u
// Returns false otherwise
bool dominates(int v, int u, std::set<int> X) {
	return set_S(N_C(v, X), N_C(u, X));
}

// Returns the maximum independent set including only X vertices
// that has exactly one element of S (|S| = 2)
std::set<int> MIS1(std::set<int> X, std::set<int> S) {
	auto it1 = S.begin();
	int s1 = (*it1);
	std::advance(it1, 1);
	int s2 = (*it1);
	std::map<int, int> deg = degrees(X);

	// To make sure d(s1) <= d(s2)
	if (deg[s1] > deg[s2]) {
		int aux = s2;
		s2 = s1;
		s1 = aux;
	}

	if (deg[s1] <= 1)
		return MIS(X);

	if (edge(s1, s2)) {
		if (deg[s1] <= 3)
			return MIS(X);
		return bigst_set(set_U(MIS(set_M(X, N_C(s1, X))), { s1 }), set_U(MIS(set_M(X, N_C(s2, X))), { s2 }));
	}

	if (!set_I(N(s1, X), N(s2, X)).empty())
		return MIS1(set_M(X, set_I(N(s1, X), N(s2, X))), S);

	if (deg[s2] == 2) {
		std::set<int> Ns1 = N(s1, X);
		auto it2 = Ns1.begin();
		int e = *it2;
		std::advance(it2, 1);
		int f = *it2;

		if (edge(e, f))
			return set_U(MIS(set_M(X, N_C(s1, X))), {s1});

		if (set_S(set_M(set_U(N(e, X), N(f, X)), {s1}), N(s2, X)))
			return set_U(MIS(set_M(X, set_U(N_C(s1, X), N_C(s2, X)))), {e, f, s2});
		
		return bigst_set(set_U(MIS(set_M(X, N_C(s1, X))), { s1 }), set_U(MIS(set_M(X, set_U(N_C(e, X), set_U(N_C(f, X), N_C(s2, X))))), {e, f, s2}));
	}

	return bigst_set(set_U(MIS(set_M(X, N_C(s2, X))), { s2 }), set_U(MIS2(set_M(X, set_U(N_C(s1, X), {s2})), N(s2, X)), {s1}));
}

// Returns the maximum independent set including only X vertices
// with at least two elements of S
std::set<int> MIS2(std::set<int> X, std::set<int> S) {
	std::map<int, int> deg = degrees(X);
	std::vector<std::pair<int, int>> sorted_degrees; // We use vector of pair to sort the degree map
	for (auto& it : deg)
		sorted_degrees.push_back(it);
	std::sort(sorted_degrees.begin(), sorted_degrees.end(), cmp_second);


	std::set<int> result;
	if (S.size() <= 1)
		return result;

	auto it = sorted_degrees.begin();
	int s1 = (*it).first;
	advance(it, 1);
	int s2 = (*it).first;

	if (S.size() == 2) {
		if (edge(s1, s2))
			return result;
		return set_U(MIS(set_M(X, set_U(N_C(s1, X), N_C(s2, X)))), { s1, s2 });
	}

	advance(it, 1);
	int s3 = (*it).first;

	if (S.size() == 3) {
		if (deg[s1] == 0)
			set_U(MIS1(set_M(X, { s1 }), set_M(S, {s1})), { s1 });
		
		if (edge(s1, s2) && edge(s2, s3) && edge(s3, s1))
			return result;

		if (edge(s1, s2) && edge(s1, s3))
			return set_U(MIS(set_M(X, set_U(N_C(s2, X), N_C(s3, X)))), {s2, s3});

		if (edge(s2, s1) && edge(s2, s3))
			return set_U(MIS(set_M(X, set_U(N_C(s1, X), N_C(s3, X)))), { s1, s3 });
		
		if (edge(s3, s1) && edge(s3, s2))
			return set_U(MIS(set_M(X, set_U(N_C(s1, X), N_C(s2, X)))), { s1, s2 });

		if (edge(s1, s2))
			return set_U(MIS1(set_M(X, N_C(s3, X)), {s1, s2}), {s3});

		if (edge(s2, s3))
			return set_U(MIS1(set_M(X, N_C(s1, X)), { s2, s3 }), { s1 });

		if (edge(s3, s1))
			return set_U(MIS1(set_M(X, N_C(s2, X)), { s1, s3 }), { s2 });

		std::set<int> intersec;

		intersec = set_I(N(s1, X), N(s2, X));
		if (!intersec.empty())
			return MIS2(set_M(X, { *(intersec.begin()) }), S);

		intersec = set_I(N(s2, X), N(s3, X));
		if (!intersec.empty())
			return MIS2(set_M(X, { *(intersec.begin()) }), S);

		intersec = set_I(N(s1, X), N(s3, X));
		if (!intersec.empty())
			return MIS2(set_M(X, { *(intersec.begin()) }), S);

		if (deg[s1] == 1)
			return set_U(MIS1(set_M(X, N_C(s1, X)), set_M(S, { s1 })), { s1 });

		return bigst_set(set_U(MIS1(set_M(X, N_C(s1, X)), set_M(S, {s1})), { s1 }), MIS2(set_M(X, set_U(set_U(N_C(s2, X), N_C(s3, X)), {s1})), N(s1, X)));
	}

	if (S.size() == 4) {
		 // If exists v with d(v) <= 3
		if (sorted_degrees[0].second <= 3)
			return MIS(X);
		
		return bigst_set(set_U(MIS(set_M(X, N_C(s1, X))), { s1 }), MIS2(set_M(X, { s1 }), set_M(S, {s1})));
	}

	return MIS(X);
}

// Returns the maximum independent set including only X vertices
std::set<int> MIS(std::set<int> X) {
	std::set<int> result;

	if (X.empty())
		return result;

	// We can unite maximum independent sets
	// of disconnected components 
	std::vector<std::set<int>> components = connected_components(X);
	if (components.size() > 1) {
		for (auto i = components.begin(); i != components.end(); i++)
			result = set_U(result, MIS(*i));
		
		return result;
	}

	if (X.size() <= 2) {
		return {*X.begin()};
	}

	std::map<int, int> deg = degrees(X);
	std::vector<std::pair<int, int>> sorted_degrees; // We use vector of pair to sort the degree map
	for (auto& it : deg)
		sorted_degrees.push_back(it);
	std::sort(sorted_degrees.begin(), sorted_degrees.end(), cmp_second);

	// Picking the minimal degree vertex
	int v = sorted_degrees[0].first;

	// Picking the maximal degree neighboor of v
	int u;
	for (auto it = sorted_degrees.rbegin(); it != sorted_degrees.rend(); it++) {
		u = (*it).first;
		if (edge(v, u))
			break;
	}

	if (deg[v] == 1) 
		return set_U({v}, MIS(set_M(X, N_C(v, X))));

	if (deg[v] == 2) {
		int u2 = *(set_M(N(v, X), { u }).begin());
		if (edge(u, u2))
			return set_U({ v }, MIS(set_M(X, N_C(v, X))));
		else
			return bigst_set(set_U({u, u2}, MIS(set_M(X, set_U(N_C(u, X), N_C(u2, X))))), set_U({v}, MIS2(set_M(X, N_C(v, X)), N2(v, X))));
	}

	if (deg[v] == 3)
		return bigst_set(MIS2(set_M(X, {v}), N(v, X)), set_U({v}, MIS(set_M(X, N_C(v, X)))));

	if (dominates(v, u, X))
		return MIS(set_M(X, {u}));

	return bigst_set(MIS(set_M(X, {u})), set_U({u}, MIS(set_M(X, N_C(u, X)))));
}

// Input the number of vertices and adjacency matrix
// Prints the maximum independent set
int main(int argc, char** argv) {
	if (argc > 1){
		graphAdjMatrixFromFile(argv[1]);
		
		// Building the set of vertices
		std::set<int> X;
		for (int i = 0; i < N_vertices; i++)
			X.insert(i);

		std::set<int> max_set = MIS(X);
		
		// Printing the maximum independent set
		std::cout << "Maximum Independent Set = {";
		for (auto it = max_set.begin(); it != std::prev(max_set.end()); it++)
			std::cout << *it << ", ";
		if (!max_set.empty())
			std::cout << *(max_set.rbegin());
		std::cout << "}\n";
		
		std::cout << "Maximum Independent Set Cardinality = " << max_set.size()<<"\n";
		
		free(mat);
	}
	return 0;
}