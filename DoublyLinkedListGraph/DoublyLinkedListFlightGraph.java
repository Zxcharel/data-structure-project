package DoublyLinkedListGraph;


import java.util.*;
import common.Edge;
import common.FlightGraphInterface;

/**
 * Doubly-linked list implementation of a flight graph.
 * Each airport is a node in a doubly linked list (header + trailer),
 * and each airport node has its own doubly linked list of outgoing flights.
 */
public class DoublyLinkedListFlightGraph implements FlightGraphInterface {

    // Sentinel nodes for airports list
    private AirportNode header;
    private AirportNode trailer;
    private int airportCount;

    // Inner class for airport nodes
    private class AirportNode {
        String airportCode;
        AirportNode prev;
        AirportNode next;
        EdgeList edgeList;

        AirportNode(String code) {
            this.airportCode = code.toLowerCase();
            this.edgeList = new EdgeList();
        }
    }

    // Inner class for doubly linked list of edges
    private class EdgeList {
        EdgeNode header;
        EdgeNode trailer;
        int size;

        EdgeList() {
            header = new EdgeNode(null);
            trailer = new EdgeNode(null);
            header.next = trailer;
            trailer.prev = header;
            size = 0;
        }

        private class EdgeNode {
            Edge edge;
            EdgeNode prev;
            EdgeNode next;

            EdgeNode(Edge e) {
                this.edge = e;
            }
        }

        void addEdge(Edge edge) {
            EdgeNode node = new EdgeNode(edge);
            node.next = trailer;
            node.prev = trailer.prev;
            trailer.prev.next = node;
            trailer.prev = node;
            size++;
        }

        List<Edge> toList() {
            List<Edge> list = new ArrayList<>();
            EdgeNode current = header.next;
            while (current != trailer) {
                list.add(current.edge);
                current = current.next;
            }
            return list;
        }

        Edge getBestFlight(String dest) {
            Edge best = null;
            EdgeNode current = header.next;
            while (current != trailer) {
                if (current.edge.getDestinationAirport().equalsIgnoreCase(dest)) {
                    if (best == null || current.edge.getWeight() < best.getWeight()) {
                        best = current.edge;
                    }
                }
                current = current.next;
            }
            return best;
        }
    }

    public DoublyLinkedListFlightGraph() {
        header = new AirportNode(null);
        trailer = new AirportNode(null);
        header.next = trailer;
        trailer.prev = header;
        airportCount = 0;
    }

    private AirportNode findAirportNode(String code) {
        code = code.toLowerCase();
        AirportNode current = header.next;
        while (current != trailer) {
            if (current.airportCode.equals(code)) return current;
            current = current.next;
        }
        return null;
    }

    @Override
    public void addAirport(String airportCode) {
        if (findAirportNode(airportCode) != null) return;
        AirportNode node = new AirportNode(airportCode);
        node.prev = trailer.prev;
        node.next = trailer;
        trailer.prev.next = node;
        trailer.prev = node;
        airportCount++;
    }

    @Override
    public void addFlight(String source, String dest, String airline, double weight) {
        addAirport(source);
        addAirport(dest);
        AirportNode srcNode = findAirportNode(source);
        srcNode.edgeList.addEdge(new Edge(dest, airline, weight));
    }

    @Override
    public void addFlightWithRatings(String source, String dest, String airline,
                                     double overallRating, double valueForMoneyRating,
                                     double inflightEntertainmentRating, double cabinStaffRating,
                                     double seatComfortRating) {
        double combined = Edge.calculateCombinedWeight(overallRating, valueForMoneyRating,
                inflightEntertainmentRating, cabinStaffRating, seatComfortRating);
        addFlight(source, dest, airline, combined);
    }

    @Override
    public List<Edge> getFlightsFrom(String airportCode) {
        AirportNode node = findAirportNode(airportCode);
        return node != null ? node.edgeList.toList() : new ArrayList<>();
    }

    @Override
    public List<Edge> getFlightsTo(String dest) {
        List<Edge> result = new ArrayList<>();
        AirportNode current = header.next;
        while (current != trailer) {
            Edge best = current.edgeList.getBestFlight(dest);
            if (best != null) result.add(best);
            current = current.next;
        }
        return result;
    }

    @Override
    public Edge getBestFlight(String source, String dest) {
        AirportNode node = findAirportNode(source);
        return node != null ? node.edgeList.getBestFlight(dest) : null;
    }

    @Override
    public Set<String> getAllAirports() {
        Set<String> set = new LinkedHashSet<>();
        AirportNode current = header.next;
        while (current != trailer) {
            set.add(current.airportCode);
            current = current.next;
        }
        return set;
    }

    @Override
    public boolean hasAirport(String airportCode) {
        return findAirportNode(airportCode) != null;
    }

    @Override
    public int getAirportCount() {
        return airportCount;
    }

    @Override
    public int getFlightCount() {
        int count = 0;
        AirportNode current = header.next;
        while (current != trailer) {
            count += current.edgeList.size;
            current = current.next;
        }
        return count;
    }

    @Override
    public Set<String> getAirlinesFrom(String airportCode) {
        Set<String> set = new LinkedHashSet<>();
        AirportNode node = findAirportNode(airportCode);
        if (node != null) {
            for (Edge e : node.edgeList.toList()) set.add(e.getAirline());
        }
        return set;
    }

    @Override
    public void printGraphStats() {
        System.out.println("Doubly Linked List Flight Graph Stats:");
        System.out.println("  - Airports: " + getAirportCount());
        System.out.println("  - Flights: " + getFlightCount());
    }

    @Override
    public void printGraph() {
        AirportNode current = header.next;
        while (current != trailer) {
            System.out.println(current.airportCode + " -> " + current.edgeList.toList());
            current = current.next;
        }
    }

    @Override
    public void clear() {
        header.next = trailer;
        trailer.prev = header;
        airportCount = 0;
    }
}
