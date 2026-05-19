import java.util.*;

// ─────────────────────────────────────────────
//  GRAPH
// ─────────────────────────────────────────────
class Graph {
    private final int nodes;
    private final List<List<int[]>> adjList; // [neighbor, travelTime]

    Graph(int nodes) {
        this.nodes = nodes;
        adjList = new ArrayList<>();
        for (int i = 0; i < nodes; i++) adjList.add(new ArrayList<>());
    }

    void addEdge(int u, int v, int time) {
        adjList.get(u).add(new int[]{v, time});
        adjList.get(v).add(new int[]{u, time}); // undirected
    }

    // Dijkstra: returns min time from 'source' to all nodes
    int[] dijkstra(int source) {
        int[] dist = new int[nodes];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[source] = 0;

        // PriorityQueue: [distance, node]
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[0]));
        pq.offer(new int[]{0, source});

        while (!pq.isEmpty()) {
            int[] curr = pq.poll();
            int currDist = curr[0], currNode = curr[1];

            if (currDist > dist[currNode]) continue; // stale entry

            for (int[] neighbor : adjList.get(currNode)) {
                int nextNode = neighbor[0], edgeTime = neighbor[1];
                if (dist[currNode] + edgeTime < dist[nextNode]) {
                    dist[nextNode] = dist[currNode] + edgeTime;
                    pq.offer(new int[]{dist[nextNode], nextNode});
                }
            }
        }
        return dist;
    }
}

// ─────────────────────────────────────────────
//  TAXI
// ─────────────────────────────────────────────
class Taxi {
    int id;
    int location;
    boolean available;
    int timeWhenFree; // global timer time when taxi will be free

    Taxi(int id, int location) {
        this.id = id;
        this.location = location;
        this.available = true;
        this.timeWhenFree = 0;
    }

    @Override
    public String toString() {
        return "Taxi-" + id + " [location=" + location + ", available=" + available + ", freeAt=" + timeWhenFree + "]";
    }
}

// ─────────────────────────────────────────────
//  CAB ALLOCATION SYSTEM
// ─────────────────────────────────────────────
class CabAllocationSystem {

    private final Graph graph;
    private final List<Taxi> taxis;
    private int globalTime; // simulated global clock

    CabAllocationSystem(Graph graph) {
        this.graph = graph;
        this.taxis = new ArrayList<>();
        this.globalTime = 0;
    }

    void addTaxi(Taxi taxi) {
        taxis.add(taxi);
    }

    void advanceTime(int time) {
        globalTime += time;
        // Update taxi availability based on global time
        for (Taxi taxi : taxis) {
            if (!taxi.available && globalTime >= taxi.timeWhenFree) {
                taxi.available = true;
            }
        }
        System.out.println("\n⏱ Global time advanced to: " + globalTime);
    }

    void requestRide(int userLocation, int destination) {
        System.out.println("\n📍 User at node " + userLocation + " wants to go to node " + destination);
        System.out.println("   Current global time: " + globalTime);

        Taxi bestTaxi = null;
        int bestArrivalTime = Integer.MAX_VALUE;

        for (Taxi taxi : taxis) {
            // Dijkstra from taxi's current location
            int[] distFromTaxi = graph.dijkstra(taxi.location);
            int timeToReachUser = distFromTaxi[userLocation];

            if (timeToReachUser == Integer.MAX_VALUE) continue; // unreachable

            int arrivalTime;

            if (taxi.available) {
                // Scenario 1: Taxi is free now → arrives at globalTime + travel time
                arrivalTime = globalTime + timeToReachUser;
            } else {
                // Scenario 2: Taxi is busy → it's free at timeWhenFree, then travels to user
                arrivalTime = taxi.timeWhenFree + timeToReachUser;
            }

            System.out.println("   Taxi-" + taxi.id +
                    " | available=" + taxi.available +
                    " | freeAt=" + taxi.timeWhenFree +
                    " | travelToUser=" + timeToReachUser +
                    " | arrivalTime=" + arrivalTime);

            if (arrivalTime < bestArrivalTime) {
                bestArrivalTime = arrivalTime;
                bestTaxi = taxi;
            }
        }

        if (bestTaxi == null) {
            System.out.println("❌ No taxi available at this time.");
            return;
        }

        // Assign the best taxi
        int[] distFromTaxi = graph.dijkstra(bestTaxi.location);
        int tripTime = distFromTaxi[destination];   // time from user to destination
        // Note: we use arrival time as base, then add trip time
        int[] distFromUser = graph.dijkstra(userLocation);
        int userToDestTime = distFromUser[destination];

        bestTaxi.available = false;
        bestTaxi.timeWhenFree = bestArrivalTime + userToDestTime;
        bestTaxi.location = destination;

        System.out.println("\n✅ Assigned Taxi-" + bestTaxi.id +
                " | Arrives at user at time=" + bestArrivalTime +
                " | Trip ends at time=" + bestTaxi.timeWhenFree +
                " | New location=node " + destination);
    }

    void printAllTaxis() {
        System.out.println("\n🚕 All Taxis Status (globalTime=" + globalTime + "):");
        for (Taxi taxi : taxis) System.out.println("   " + taxi);
    }
}

// ─────────────────────────────────────────────
//  MAIN
// ─────────────────────────────────────────────
public class Main {

    public static void main(String[] args) {

        /*
         *  City Graph (7 nodes: 0-6)
         *
         *   0 ---5--- 1 ---3--- 2
         *   |         |         |
         *   4         2         6
         *   |         |         |
         *   3 ---1--- 4 ---4--- 5
         */

        Graph city = new Graph(7);
        city.addEdge(0, 1, 5);
        city.addEdge(1, 2, 3);
        city.addEdge(0, 3, 4);
        city.addEdge(1, 4, 2);
        city.addEdge(2, 5, 6);
        city.addEdge(3, 4, 1);
        city.addEdge(4, 5, 4);

        CabAllocationSystem system = new CabAllocationSystem(city);

        // Initialize 4 taxis at different city nodes
        system.addTaxi(new Taxi(1, 0));
        system.addTaxi(new Taxi(2, 3));
        system.addTaxi(new Taxi(3, 5));
        system.addTaxi(new Taxi(4, 2));

        system.printAllTaxis();

        // ── Request 1 ──
        system.requestRide(4, 2);   // User at node 4, wants to go to node 2

        // ── Advance time ──
        system.advanceTime(5);

        // ── Request 2 ──
        system.requestRide(1, 5);   // User at node 1, wants to go to node 5

        // ── Advance time ──
        system.advanceTime(10);

        // ── Request 3 ──
        system.requestRide(3, 0);   // User at node 3, wants to go to node 0

        system.printAllTaxis();
    }
}