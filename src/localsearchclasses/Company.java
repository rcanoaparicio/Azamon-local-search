package localsearchclasses;

import IA.Azamon.Oferta;
import IA.Azamon.Paquete;
import IA.Azamon.Paquetes;
import IA.Azamon.Transporte;

import aima.search.framework.Successor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Date;

//TODO NEXT STEPS:
// Create the move operator
// Create the swap operator
// Create initial solution: no random, no greedy $
// Do implement: highest priority first linearly (or remembering last filled) to whatever
//               highest priority first linearly (or remembering last filled) to the cheapest (implies multiple sub-sorts)

//TODO
// Experiment 1: choose best operators, using only 1 of the initial solution strategies. To choose look at heuristic value.
// Experiment 2: Choose best initial solution. To choose look at heuristic

public class Company {

    //The problem to solve, order must be final once initial solution computation has started
    static Paquetes paquetes;
    static Transporte transporte;

    //for heuristic calculation
    static double maxCost;
    static int maxHappyness;

    //Some constants to avoid magic numbers
    static final int fastMinTime = 1;
    static final int fastMaxTime = 1;
    static final double fastPriorityCost = 5; //1 day

    static final int mediumMinTime = 2;
    static final int mediumMaxTime = 3;
    static final double mediumPriorityCost = 3; //2-3 days

    static final int slowMinTime = 4;
    static final int slowMaxTime = 5;
    static final double slowPriorityCost = 1.5; //4-5 days

    static final int slowStorageTime = 2; //for 5 day deliveries
    static final int fastStorageTime = 1; //for 3-4 day deliveries

    static final double dailyStorageCost = 0.25;

    static Random randomNumberGenerator = new Random((int) new Date().getTime());

    //Our current solution state
    int[] packageAssignments; //Same length as paquetes, also contiguous memory for the win!
    double[] freeOfferSpace; //Same length as transporte
    double storageCost = 0; //The only reason these costs are separated is because they are both requested
    double sendCost = 0;
    int happyness = 0;

    //GETTERS
    public double getStorageCost() {
        return storageCost;
    }

    public double getSendCost() {
        return sendCost;
    }

    public int getHappyness() {
        return happyness;
    }

    //CONSTRUCTORS

    public Company() {
        packageAssignments = new int[paquetes.size()]; //Same length as paquetes
        freeOfferSpace = new double[transporte.size()]; //Same length as transporte

        /*
        Misc.insertionSortArrayList(transporte);
        for (Oferta of : transporte) {
            System.out.println(of.toString());
        }
        transporte.sort((t1, t2) -> {
            if (t1.getDias() == t2.getDias()) return Double.compare(t1.getPrecio(), t2.getPrecio());
            else return Integer.compare(t1.getDias(), t2.getDias());
        });
         */

        Arrays.fill(packageAssignments, -1); //0 is a valid value so initialize to invalid values
        int index = 0;
        for (Oferta offer : transporte) {
            freeOfferSpace[index] = offer.getPesomax();
            index++;
        }

        for (Paquete packs : paquetes) {  //Compute static money gained from clients
            int expectedDate = priorityToMinExpectDate(packs.getPrioridad());
            switch (expectedDate) {  //The client pays the same regardless of whether we send it earlier or not
                case 1:
                    sendCost -= fastPriorityCost;
                    break;
                case 2:
                    sendCost -= mediumPriorityCost;
                    break;
                case 4:
                    sendCost -= slowPriorityCost;
                    break;
                default:
                    throw new RuntimeException("Impossible case reached");
            }
        }
        generateInitialSolution();

/*
        for (int i = 0; i < paquetes.size(); i++) {
            System.out.println(packageAssignments[i]);
        }
        for (int i = 0; i < transporte.size(); i++) {
            System.out.println(freeOfferSpace[i]);
        }
 */
    }

    public Company(Company company) {  //Deep copy constructor
        this.packageAssignments = company.packageAssignments.clone();
        this.freeOfferSpace = company.freeOfferSpace.clone();
        this.storageCost = company.storageCost;
        this.sendCost = company.sendCost;
        this.happyness = company.happyness;
    }

    //INITIAL SOLUTION GENERATORS

    private void assignPackagesNoRetries() { //Cheaper, less compact
        int j = 0;
        for (int i = 0; i < paquetes.size(); i++) {
            double weight = paquetes.get(i).getPeso();
            for (int k = j; k < transporte.size(); ++k) {
                if (weight <= freeOfferSpace[k]) {
                    assignPackage(i, k);
                    j = k;
                    break;
                }
            }

            if (packageAssignments[i] == -1) {
                throw new RuntimeException("Could not generate initial solution with retries");
            }
        }
    }

    private void assignPackagesRetries() {  //More expensive, more compact
        for (int i = 0; i < paquetes.size(); i++) {
            double weight = paquetes.get(i).getPeso();
            for (int j = 0; j < transporte.size(); ++j) {
                if (weight <= freeOfferSpace[j]) {
                    assignPackage(i, j);
                    break;
                }
            }

            if (packageAssignments[i] == -1) {
                throw new RuntimeException("Could not generate initial solution with retries");
            }
        }
    }

    private void generateInitialSolution() {
        paquetes.sort(Comparator.comparingInt(Paquete::getPrioridad));
        //paquetes.sort((p1, p2) -> {
        //    if (p1.getPrioridad() == p2.getPrioridad()) return Double.compare(p1.getPeso(), p2.getPeso());
        //    else return Integer.compare(p1.getPrioridad(), p2.getPrioridad());
        assignPackagesRetries();
    }

    //SUCCESSOR GENERATION

    //Hill
    public List getAllSuccessors() {
        return generateAllMoves();
    }

    //SA
    public List randomSuccessor() {
        return generateRandomMove();
    }

    //OPERATOR GENERATORS

    private ArrayList<Successor> generateAllMoves() {
        ArrayList<Successor> successors = new ArrayList<>();

        //Try to move all packages
        for (int i = 0; i < packageAssignments.length; ++i) {
            //Collect some initial required info
            int currentOfferIndex = packageAssignments[i];
            Paquete paq = paquetes.get(i);
            double weight = paq.getPeso();
            int maxDueDate = priorityToMaxExpectDate(paq.getPrioridad());

            //Try to move it to all offers
            for (int j = 0; j < transporte.size(); ++j) {
                if (freeOfferSpace[j] < weight) continue; //No space
                if (transporte.get(j).getDias() > maxDueDate)
                    break; //Offers are sorted by delivery date, any offer after this one will be too late
                if (currentOfferIndex == j) continue; //Can't move to itself

                Company newCompany = new Company(this);
                newCompany.move(i, j);
                successors.add(new Successor("Move package " + i + " from offer " + currentOfferIndex + " to " + j, newCompany));
            }
        }
        return successors;
    }

    private List generateRandomMove() {
        ArrayList<Successor> successors = new ArrayList<>();
        while (true) {
            int i = randomNumberGenerator.nextInt(packageAssignments.length);
            int j = randomNumberGenerator.nextInt(freeOfferSpace.length);

            int currentOfferIndex = packageAssignments[i];
            Paquete paq = paquetes.get(i);
            double weight = paq.getPeso();
            int maxDueDate = priorityToMaxExpectDate(paq.getPrioridad());

            if (freeOfferSpace[j] < weight) continue; //No space
            if (transporte.get(j).getDias() > maxDueDate) continue; //Too late
            if (currentOfferIndex == j) continue; //Can't move to itself

            Company newCompany = new Company(this);
            newCompany.move(i, j);
            successors.add(new Successor("Move package " + i + " from offer " + currentOfferIndex + " to " + j, newCompany));
            return successors;
        }
    }

    private ArrayList<Successor> generateAllSwaps() {
        ArrayList<Successor> successors = new ArrayList<>();

        //Try to move swap packages
        for (int i = 0; i < packageAssignments.length; ++i) {
            //Collect some initial required info from the first package
            int offerAIndex = packageAssignments[i];
            Paquete packageA = paquetes.get(i);
            double weightA = packageA.getPeso();
            int maxDueDateA = priorityToMaxExpectDate(packageA.getPrioridad());

            //Try to swap to all other offers
            for (int j = i + 1; j < packageAssignments.length; ++j) { //Check will all remainging packages
                //Collect some initial required info from the second package
                int offerBIndex = packageAssignments[j];
                Paquete packageB = paquetes.get(j);
                double weightB = packageB.getPeso();
                int maxDueDateB = priorityToMaxExpectDate(packageB.getPrioridad());

                if (transporte.get(offerAIndex).getDias() > maxDueDateB) continue; //Too late
                if (transporte.get(offerBIndex).getDias() > maxDueDateA) continue; //Too late
                if (freeOfferSpace[offerAIndex] + weightA - weightB < 0) continue; //No space
                if (freeOfferSpace[offerBIndex] - weightA + weightB < 0) continue; //No space
                if (offerAIndex == offerBIndex) continue; //Can't swap in the same offer

                Company newCompany = new Company(this);
                newCompany.swap(i, j);
                successors.add(new Successor("Swap packages " + i + " and " + j + " between " + offerAIndex + " and " + offerBIndex, newCompany));
            }
        }
        return successors;
    }

    private ArrayList<Successor> generateAllMovesAndSwaps() {
        ArrayList<Successor> everything = generateAllMoves();
        everything.addAll(generateAllSwaps());
        return everything;
    }

    //OPERATORS

    private void move(int movingPackage, int destinationOffer) {
        undoAssignation(movingPackage);
        assignPackage(movingPackage, destinationOffer);
    }

    private void swap(int packageA, int packageB) {
        int offerA = packageAssignments[packageA];
        int offerB = packageAssignments[packageB];

        if (offerA == offerB) {
            throw new RuntimeException("Swapping between the same offer is not allowed");
        }
        undoAssignation(packageA);
        undoAssignation(packageB);
        assignPackage(packageA, offerB);
        assignPackage(packageB, offerA);
    }

    //MISC METHODS

    private void undoAssignation(int packageIndex) {
        //Get all necessary info
        Paquete paquete = paquetes.get(packageIndex);
        double weight = paquete.getPeso();
        int expectedMinDay = priorityToMinExpectDate(paquete.getPrioridad());

        int offerIndex = packageAssignments[packageIndex];
        Oferta offer = transporte.get(offerIndex);
        double priceRatio = offer.getPrecio();
        int deliveryDate = offer.getDias();

        //Undo the assignation
        storageCost -= computeStorageCost(deliveryDate, weight);
        sendCost -= computeSendCost(priceRatio, weight);
        //happyness -= computeHappyness(deliveryDate, expectedMinDay);
        packageAssignments[packageIndex] = -1;
        freeOfferSpace[offerIndex] += weight;

        if (offer.getPesomax() < freeOfferSpace[offerIndex]) {
            throw new RuntimeException("Offer ended up with more weight than it possibly could!");
        }
    }

    private void assignPackage(int packageIndex, int offerIndex) {
        if (packageAssignments[packageIndex] != -1) {
            throw new RuntimeException("Package to assign is already assigned");
        }

        //Get all necessary info
        Paquete paquete = paquetes.get(packageIndex);
        double weight = paquete.getPeso();
        int priority = paquete.getPrioridad();
        int expectedMaxDay = priorityToMaxExpectDate(priority);
        int expectedMinDay = priorityToMinExpectDate(priority);

        Oferta offer = transporte.get(offerIndex);
        double priceRatio = offer.getPrecio();
        int deliveryDate = offer.getDias();

        if (deliveryDate > expectedMaxDay) {
            throw new RuntimeException("Package assigned to an offer that arrives too late!");
        }

        //Assign
        storageCost += computeStorageCost(deliveryDate, weight);
        sendCost += computeSendCost(priceRatio, weight); //Could also be maxday, same result
        //happyness += computeHappyness(deliveryDate, expectedMinDay);
        packageAssignments[packageIndex] = offerIndex;
        freeOfferSpace[offerIndex] -= weight;

        if (freeOfferSpace[offerIndex] < 0) {
            throw new RuntimeException("Package assigned to an offer without enough space!");
        }
    }

    private static int priorityToMaxExpectDate(int priority) {
        switch (priority) {
            case 0:
                return fastMaxTime;
            case 1:
                return mediumMaxTime;
            case 2:
                return slowMaxTime;
        }
        throw new RuntimeException("Impossible case reached");
    }

    private static int priorityToMinExpectDate(int priority) {
        switch (priority) {
            case 0:
                return fastMinTime;
            case 1:
                return mediumMinTime;
            case 2:
                return slowMinTime;
        }
        throw new RuntimeException("Impossible case reached");
    }

    private static double computeHappyness(int deliveryDate, int expectedDate) {
        if (expectedDate < deliveryDate) {
            throw new RuntimeException("Attempting to deliver a package later than allowed!");
        }
        return expectedDate - deliveryDate;
    }

    private static double computeSendCost(double pricePerKg, double weight) {
        return Math.round(pricePerKg * weight * 1000.0) / 1000.0;
    }

    private static double computeStorageCost(int deliveryDate, double packageWeight) {
        if (deliveryDate == 5) return Math.round(slowStorageTime * dailyStorageCost * packageWeight * 1000.0) / 1000.0;
        else if (deliveryDate == 3 || deliveryDate == 4) {
            return Math.round(fastStorageTime * dailyStorageCost * packageWeight * 1000.0) / 1000.0;
        } else return 0;
    }


    //HEURISTIC

    public double heuristic() {
        if (storageCost + sendCost > maxCost) {
            throw new RuntimeException("cost exceeds maxCost which should be impossible");
        }
        //if (happyness > maxHappyness) {
        //    throw new RuntimeException("happyness exceeds maxHappyness which should be impossible");
        //}

        double costHeuristic = Misc.entropy(storageCost + sendCost, maxCost);
        return costHeuristic;

        //TODO when experiment 6 has been reached
        //double happynessHeuristic = - Misc.entropy(happyness,maxHappyness);
        //return costHeuristic + happynessHeuristic; //TODO allow configurable weights?
    }

    //INITIALIZATION METHODS

    public static void init(int seed, int npaq, double proportion) {
        paquetes = new Paquetes(npaq, seed);
        transporte = new Transporte(paquetes, proportion, seed); //TODO different seed than paquetes?
        computeMaxCost(npaq);
        computeMaxHappyness();

        /*
        for (Paquete paq : paquetes) {
            System.out.println(paq.toString());
        }

        for (Oferta of : transporte) {
            System.out.println(of.toString());
        }

        System.out.println(maxCost);
        System.out.println(maxHappyness);
         */
    }

    private static void computeMaxHappyness() {
        for (Paquete paq : paquetes) {
            maxHappyness += Math.abs(1 - priorityToMinExpectDate(paq.getPrioridad()));
        }
    }

    private static void computeMaxCost(int npaq) {
        double totalWeight = 0;
        for (Paquete paq : paquetes) {
            totalWeight += paq.getPeso();
        }

        double maxPrice = 0;
        for (Oferta of : transporte) {
            double cost = of.getPrecio();
            if (cost > maxPrice) maxPrice = cost;
        }

        //Assume the absolute worst (and impossible) cost.
        //This cost would never realistically happen but guarantees that our heuristic function works
        //This is not an issue considering it's not really problematic we don't use the entire range of the output
        maxCost = totalWeight * maxPrice   //Assign all packages to the most expensive offer
                + fastPriorityCost * npaq  //Use most expensive sending cost for all packages
                + npaq * dailyStorageCost * slowStorageTime  //Assume they are also stored for 2 days
                - npaq * slowPriorityCost; //Clients payed the least amount of cash possible for the delivery
    }

    public String toString() {
        String ret;
        ret = "Solution storage cost: " + getStorageCost() + "\n";
        ret += "Solution transport cost: " + getSendCost() + "\n";
        ret += "Solution total cost: " + (getStorageCost() + getSendCost()) + "\n";
        ret += "Solution happyness: " + getHappyness();
        return ret;
    }
}
