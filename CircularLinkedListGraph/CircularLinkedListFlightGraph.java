package CircularLinkedListGraph;

import common.Edge;
import common.FlightGraphInterface;
import java.util.*;

/**
 * Circular linked list implementation of a flight graph.
 * Each airport is represented as a node in a circular linked list,
 * and each node has its own circular linked list of outgoing flights.
 */
public class CircularLinkedListFlightGraph implements FlightGraphInterface {

    /** Represents an airport node in the circular linked list */
    private class AirportNode {
        String airportName;
        FlightNode flightHead; // circular list of outgoing flights
        AirportNode next;

        AirportNode(String name) {
            this.airportName = name;
            this.next = this; // circular link
        }
    }

    /** Represents a flight edge node in a circular list */
    private class FlightNode {
        Edge flight;
        FlightNode next;

        FlightNode(Edge flight) {
            this.flight = flight;
            this.next = this;
        }
    }

    private AirportNode head; // circular linked list head
    private int airportCount = 0;

    // === Utility Methods ===
    private AirportNode findAirport(String airport) {
        if (head == null) return null;
        AirportNode curr = head;
        do {
            if (curr.airportName.equalsIgnoreCase(airport)) return curr;
            curr = curr.next;
        } while (curr != head);
        return null;
    }

    @Override
    public void addAirport(String airport) {
        if (findAirport(airport) != null) return;

        AirportNode newNode = new AirportNode(airport);
        if (head == null) {
            head = newNode;
        } else {
            AirportNode tail = head;
            while (tail.next != head) tail = tail.next;
            tail.next = newNode;
            newNode.next = head;
        }
        airportCount++;
    }

    @Override
    public void addFlight(String from, String to, String airline, double weight) {
        addAirport(from);
        addAirport(to);
        AirportNode fromNode = findAirport(from);

        Edge edge = new Edge(to, airline, weight);
        FlightNode newFlight = new FlightNode(edge);

        if (fromNode.flightHead == null) {
            fromNode.flightHead = newFlight;
        } else {
            FlightNode curr = fromNode.flightHead;
            while (curr.next != fromNode.flightHead) curr = curr.next;
            curr.next = newFlight;
            newFlight.next = fromNode.flightHead;
        }
    }

    @Override
    public void addFlightWithRatings(String from, String to, String airline, double distance,
                                     double duration, double cost, double delay, double rating) {
        double combined = Edge.calculateCombinedWeight(distance, duration, cost, delay, rating);
        addFlight(from, to, airline, combined);
    }

    @Override
    public List<Edge> getFlightsFrom(String airport) {
        List<Edge> flights = new ArrayList<>();
        AirportNode node = findAirport(airport);
        if (node == null || node.flightHead == null) return flights;

        FlightNode curr = node.flightHead;
        do {
            flights.add(curr.flight);
            curr = curr.next;
        } while (curr != node.flightHead);
        return flights;
    }

    @Override
    public Set<String> getAllAirports() {
        Set<String> airports = new HashSet<>();
        if (head == null) return airports;

        AirportNode curr = head;
        do {
            airports.add(curr.airportName);
            curr = curr.next;
        } while (curr != head);
        return airports;
    }

    @Override
    public boolean hasAirport(String airport) {
        return findAirport(airport) != null;
    }

    @Override
    public int getAirportCount() {
        return airportCount;
    }

    @Override
    public int getFlightCount() {
        int count = 0;
        if (head == null) return 0;
        AirportNode curr = head;
        do {
            List<Edge> flights = getFlightsFrom(curr.airportName);
            count += flights.size();
            curr = curr.next;
        } while (curr != head);
        return count;
    }

    @Override
    public List<Edge> getFlightsTo(String airport) {
        List<Edge> result = new ArrayList<>();
        if (head == null) return result;

        AirportNode curr = head;
        do {
            List<Edge> flights = getFlightsFrom(curr.airportName);
            for (Edge e : flights) {
                if (e.getDestinationAirport().equalsIgnoreCase(airport)) {
                    result.add(e);
                }
            }
            curr = curr.next;
        } while (curr != head);
        return result;
    }

    @Override
    public Edge getBestFlight(String from, String to) {
        List<Edge> flights = getFlightsFrom(from);
        Edge best = null;
        double min = Double.MAX_VALUE;
        for (Edge e : flights) {
            if (e.getDestinationAirport().equalsIgnoreCase(to) && e.getWeight() < min) {
                best = e;
                min = e.getWeight();
            }
        }
        return best;
    }

    @Override
    public Set<String> getAirlinesFrom(String airport) {
        Set<String> airlines = new HashSet<>();
        for (Edge e : getFlightsFrom(airport)) {
            airlines.add(e.getAirline());
        }
        return airlines;
    }

    @Override
    public void printGraphStats() {
        System.out.println("=== Circular Linked List Flight Graph Stats ===");
        System.out.println("Total Airports: " + getAirportCount());
        System.out.println("Total Flight Routes: " + getFlightCount());
    }

    @Override
    public void printGraph() {
        System.out.println("=== Circular Linked List Flight Graph ===");
        if (head == null) {
            System.out.println("Graph is empty.");
            return;
        }

        AirportNode curr = head;
        do {
            System.out.println(curr.airportName + ":");
            List<Edge> flights = getFlightsFrom(curr.airportName);
            if (flights.isEmpty()) {
                System.out.println("  No outgoing flights");
            } else {
                for (Edge e : flights) {
                    System.out.println("  -> " + e);
                }
            }
            curr = curr.next;
        } while (curr != head);
    }

    @Override
    public void clear() {
        head = null;
        airportCount = 0;
    }
}
