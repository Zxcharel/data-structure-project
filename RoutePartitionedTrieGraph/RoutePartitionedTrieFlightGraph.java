import java.util.*;
import common.Edge;
import common.FlightGraphInterface;

/**
 * Route-Partitioned Trie-like Flight Graph (TreeMap-backed prototype)
 *
 * - Single source of truth per-source adjacency plus incoming index
 * - For each (source|destination) route, maintains an ordered map of weight buckets
 *   for fast best-flight queries (min by weight)
 */
public class RoutePartitionedTrieFlightGraph implements FlightGraphInterface {

    private final Map<String, List<Edge>> sourceIndex;              // source -> outgoing edges
    private final Map<String, List<Edge>> destinationIndex;         // destination -> incoming edges
    private final Map<String, TreeMap<Integer, List<Edge>>> routeIndex; // "source|dest" -> weightKey -> edges
    private final Set<String> airports;                             // set of airport codes (lowercase)
    private int flightCount;

    public RoutePartitionedTrieFlightGraph() {
        this.sourceIndex = new HashMap<>();
        this.destinationIndex = new HashMap<>();
        this.routeIndex = new HashMap<>();
        this.airports = new LinkedHashSet<>();
        this.flightCount = 0;
    }

    private static String normalize(String s) {
        return s == null ? null : s.toLowerCase();
    }

    private static String routeKey(String source, String dest) {
        return normalize(source) + "|" + normalize(dest);
    }

    // Discretize weight to an integer bucket (e.g., 3 decimal places)
    private static int weightKey(double weight) {
        return (int) Math.round(weight * 1000.0);
    }

    @Override
    public void addAirport(String airportCode) {
        String code = normalize(airportCode);
        if (code == null) return;
        if (airports.add(code)) {
            sourceIndex.computeIfAbsent(code, k -> new ArrayList<>());
        }
    }

    @Override
    public void addFlight(String sourceAirport, String destinationAirport, String airline, double weight) {
        // Track airports (lowercased for set/indexing)
        addAirport(sourceAirport);
        addAirport(destinationAirport);

        // Create edge (keep destination as-is from input for readability in printing)
        Edge edge = new Edge(destinationAirport, airline, weight);

        // Outgoing index
        String src = normalize(sourceAirport);
        sourceIndex.computeIfAbsent(src, k -> new ArrayList<>()).add(edge);

        // Incoming index (normalize destination key)
        String dst = normalize(destinationAirport);
        destinationIndex.computeIfAbsent(dst, k -> new ArrayList<>()).add(edge);

        // Route-partitioned ordered index
        String key = routeKey(sourceAirport, destinationAirport);
        TreeMap<Integer, List<Edge>> ordered = routeIndex.computeIfAbsent(key, k -> new TreeMap<>());
        int wk = weightKey(weight);
        ordered.computeIfAbsent(wk, k -> new ArrayList<>()).add(edge);

        flightCount++;
    }

    @Override
    public void addFlightWithRatings(String sourceAirport, String destinationAirport, String airline,
                                     double overallRating, double valueForMoneyRating,
                                     double inflightEntertainmentRating, double cabinStaffRating,
                                     double seatComfortRating) {
        double combined = Edge.calculateCombinedWeight(
                overallRating, valueForMoneyRating, inflightEntertainmentRating, cabinStaffRating, seatComfortRating);
        addFlight(sourceAirport, destinationAirport, airline, combined);
    }

    @Override
    public List<Edge> getFlightsFrom(String airportCode) {
        String code = normalize(airportCode);
        List<Edge> list = sourceIndex.get(code);
        return list == null ? new ArrayList<>() : new ArrayList<>(list);
    }

    @Override
    public Set<String> getAllAirports() {
        return new LinkedHashSet<>(airports);
    }

    @Override
    public boolean hasAirport(String airportCode) {
        String code = normalize(airportCode);
        return airports.contains(code);
    }

    @Override
    public int getAirportCount() {
        return airports.size();
    }

    @Override
    public int getFlightCount() {
        return flightCount;
    }

    @Override
    public List<Edge> getFlightsTo(String destinationAirport) {
        String dst = normalize(destinationAirport);
        List<Edge> list = destinationIndex.get(dst);
        return list == null ? new ArrayList<>() : new ArrayList<>(list);
    }

    @Override
    public Edge getBestFlight(String sourceAirport, String destinationAirport) {
        String key = routeKey(sourceAirport, destinationAirport);
        TreeMap<Integer, List<Edge>> ordered = routeIndex.get(key);
        if (ordered == null || ordered.isEmpty()) return null;
        Map.Entry<Integer, List<Edge>> first = ordered.firstEntry();
        if (first == null || first.getValue().isEmpty()) return null;

        // Pick the lowest-weight edge in the minimal bucket (ties possible)
        Edge best = first.getValue().get(0);
        for (Edge e : first.getValue()) {
            if (e.getWeight() < best.getWeight()) best = e;
        }
        return best;
    }

    @Override
    public Set<String> getAirlinesFrom(String airportCode) {
        Set<String> set = new LinkedHashSet<>();
        for (Edge e : getFlightsFrom(airportCode)) set.add(e.getAirline());
        return set;
    }

    @Override
    public void printGraphStats() {
        System.out.println("Route-Partitioned Trie Flight Graph Stats:");
        System.out.println("  - Airports: " + getAirportCount());
        System.out.println("  - Flights: " + getFlightCount());
        System.out.println("  - Routes indexed: " + routeIndex.size());
    }

    @Override
    public void printGraph() {
        for (String airport : airports) {
            System.out.println(airport + " -> " + getFlightsFrom(airport));
        }
    }

    @Override
    public void clear() {
        sourceIndex.clear();
        destinationIndex.clear();
        routeIndex.clear();
        airports.clear();
        flightCount = 0;
    }
}


