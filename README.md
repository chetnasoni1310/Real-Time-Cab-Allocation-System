# 🚕 Real-Time Cab Allocation System

A Java-based simulation of a cab dispatch system — inspired by Ola/Uber — that assigns the optimal taxi to a user by computing the fastest arrival time using **Dijkstra's Algorithm** on a city graph.

---

## 📌 Problem Statement

Given a city modeled as a graph, a set of taxis at different locations, and a user requesting a ride — determine which taxi will reach the user the **fastest**, considering both currently available and currently busy taxis.

---

## 🧠 Core Idea

- The city is represented as a **weighted graph** where nodes are locations and edge weights are travel times.
- Each taxi has a location, availability status, and a predicted free time.
- On every ride request, **Dijkstra's Algorithm** is run from each taxi's location to find the minimum travel time to the user.
- The system evaluates **two scenarios** per taxi and picks the best overall arrival time.

---

## ⚙️ How It Works

### Static Allocation
1. User requests a ride from node `U` to node `D`
2. For every **available** taxi, run Dijkstra from taxi's location → user's location
3. Assign the taxi with the minimum travel time
4. Mark taxi as unavailable, update its location to `D`

### Dynamic Allocation (with Global Timer)
Adds a `timeWhenFree` field to each taxi and a **global clock** to the system.

On each request, two scenarios are evaluated for **every taxi** (busy or free):

| Scenario | Taxi State | Arrival Time Formula |
|----------|-----------|----------------------|
| 1 | Currently free | `globalTime + dijkstra(taxiLocation → user)` |
| 2 | Currently busy | `timeWhenFree + dijkstra(taxiFutureLocation → user)` |

The taxi with the **lowest arrival time** wins the assignment.

---

## 🗂️ Project Structure

```
CabAllocationSystem/
│
├── Graph.java                  # Adjacency list graph + Dijkstra implementation
├── Taxi.java                   # Taxi model (id, location, available, timeWhenFree)
├── CabAllocationSystem.java    # Core dispatch logic (requestRide, advanceTime)
└── Main.java                   # Demo setup and test cases
```

---

## 🔧 Classes & Responsibilities

### `Graph`
- Stores city as an **adjacency list**: `List<List<int[]>>`
- Each edge: `[neighborNode, travelTime]`
- `dijkstra(int source)` → returns `int[]` of minimum distances from source to all nodes

### `Taxi`
```java
int id              // Unique taxi identifier
int location        // Current node in the city graph
boolean available   // Is the taxi free right now?
int timeWhenFree    // Global time when taxi completes current trip
```

### `CabAllocationSystem`
- Maintains list of all taxis and the global timer
- `requestRide(int userLocation, int destination)` — core dispatch logic
- `advanceTime(int time)` — simulates time passing, updates taxi availability

---

## 🗺️ Sample City Graph

```
 0 ---5--- 1 ---3--- 2
 |         |         |
 4         2         6
 |         |         |
 3 ---1--- 4 ---4--- 5
```

Numbers on edges = travel time between locations.

---

## ▶️ Sample Output

```
🚕 All Taxis Status (globalTime=0):
   Taxi-1 [location=0, available=true,  freeAt=0]
   Taxi-2 [location=3, available=true,  freeAt=0]
   Taxi-3 [location=5, available=true,  freeAt=0]
   Taxi-4 [location=2, available=true,  freeAt=0]

📍 User at node 4 wants to go to node 2
   Taxi-1 | available=true  | freeAt=0  | travelToUser=3  | arrivalTime=3
   Taxi-2 | available=true  | freeAt=0  | travelToUser=1  | arrivalTime=1
   Taxi-3 | available=true  | freeAt=0  | travelToUser=4  | arrivalTime=4
   Taxi-4 | available=true  | freeAt=0  | travelToUser=5  | arrivalTime=5

✅ Assigned Taxi-2 | Arrives at user at time=1 | Trip ends at time=3 | New location=node 2
```

---

## 📊 Algorithm Choice — Why Dijkstra?

| Algorithm | Time Complexity | Handles Negative Weights | Use Case |
|-----------|----------------|--------------------------|----------|
| **Dijkstra** ✅ | O(E log V) | ❌ No | Single-source, non-negative weights |
| Bellman-Ford | O(V × E) | ✅ Yes | Single-source with negative weights |
| Floyd-Warshall | O(V³) | ✅ Yes | All-pairs shortest paths |

**Why Dijkstra is the right choice here:**
- Edge weights = travel time → always positive, no negatives possible
- We need **single-source** shortest path (one taxi → one user), not all-pairs
- It is the fastest correct algorithm for this exact scenario

---

## 🧱 Data Structures Used

| Structure | Where Used | Why |
|-----------|-----------|-----|
| `List<List<int[]>>` | Graph adjacency list | Space-efficient for sparse city graphs — O(V + E) |
| `PriorityQueue<int[]>` | Dijkstra's min-heap | Extracts minimum distance node in O(log V) |
| `ArrayList<Taxi>` | Taxi fleet | Simple iteration over all taxis per request |

---

## 📈 Complexity Analysis

| Operation | Time Complexity |
|-----------|----------------|
| Dijkstra (single run) | O(E log V) |
| `requestRide()` with T taxis | O(T × E log V) |
| `advanceTime()` | O(T) |
| Space (graph) | O(V + E) |

---

## 🚀 How to Run

**Prerequisites:** Java 8 or above

```bash
# Clone the repository
git clone https://github.com/your-username/cab-allocation-system.git
cd cab-allocation-system

# Compile
javac *.java

# Run
java Main
```

---

## 💡 Future Improvements

- [ ] Expose ride booking as a REST API using Spring Boot (`POST /ride`)
- [ ] Persist taxi and ride data in MySQL using Spring Data JPA
- [ ] Replace node IDs with real GPS coordinates (latitude/longitude)
- [ ] Use spatial indexing (k-d tree / geohash) to shortlist nearby taxis before running Dijkstra
- [ ] Add concurrency handling for simultaneous ride requests
- [ ] Build a frontend map visualization showing taxi movement in real time

---

## 👩‍💻 Author

**Chetna Soni**  
B.Tech Information Technology — Rajasthan Technical University  
[GitHub](https://github.com/chetnasoni1310) • [LinkedIn](https://linkedin.com/in/chetna-soni1209)
