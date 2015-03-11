/*
 * Questo programma genera catene che come rinomine hanno solo involuzioni o rinomine in se stessi
 */
package involuzioni;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

/**
 *
 * @author Matteo and Giuly
 */
public class Involuzioni {

    /*
     * This function generates a Triple ret which contains the probability
     * matrix, the pi vector and the rho vector.
     */
    public static Triple getChain(int n) {
        Triple ret = new Triple();
        //we create an additional vertex used to fix the outgoing rate of the renaming vertex 
        ret.chain = new double[n + 1][n + 1];
        ret.pi = new double[n + 1];
        ret.rho = new int[n + 1];
        //s is the set that contains all the vertices of the generated connected component
        ArrayList<Integer> s = new ArrayList();
        //u is the set that contains the remaining nodes of the graph whic aren't in s
        ArrayList<Integer> u = new ArrayList();
        ArrayList<Integer> support = new ArrayList();
        Random gen = new Random();
        int a, b;
        double sumPi = 0;
        for (int i = 0; i < n; i++) {
            support.add(i);
        }
        //Generation of the renaming function
        while (!support.isEmpty()) {
            int temp = support.get(gen.nextInt(support.size()));
            ret.rho[temp] = support.get(gen.nextInt(support.size()));
            ret.rho[ret.rho[temp]] = temp;
            support.remove((Integer) ret.rho[temp]);
            support.remove((Integer) temp);
        }
        ret.rho[n] = n;//Aditional vertex used to fix the outgoing rate of some vertices
        //generation of the pi vector
        for (int i = 0; i < n; i++) {
            if (ret.pi[i] == 0) {
                ret.pi[i] = gen.nextDouble();
            }
            ret.pi[ret.rho[i]] = ret.pi[i];
            u.add(i);
            sumPi += ret.pi[i];
        }
        ret.pi[n] = gen.nextDouble();
        sumPi += ret.pi[n];
        //All pi sum to unity
        for (int i = 0; i < ret.pi.length; i++) {
            ret.pi[i] /= sumPi;
        }
        sumPi = 0;
        for (int i = 0; i < ret.pi.length; i++) {
            sumPi += ret.pi[i];
        }
        //Add the first node to s
        s.add(u.remove(gen.nextInt(u.size())));
        //generation of edges, while u is not empty we generate an edge between a vertex of s and a vertex of u
        //then we check if u is connected to s
        while (!u.isEmpty()) {
            a = s.get(gen.nextInt(s.size()));
            b = u.remove(gen.nextInt(u.size()));
            //If the edge between a and b doesn't exist we create it
            if (ret.chain[a][b] == 0) {
                rhoBalanceEquation(a, b, ret.pi, ret.chain, ret.rho);
            }
            int x = s.get(gen.nextInt(s.size()));
            if (ret.chain[b][x] == 0) {
                //If there is no edge from b to s we create it to connect b to s
                rhoBalanceEquation(b, x, ret.pi, ret.chain, ret.rho);
            }
            //The vertex b can be added to s
            s.add(b);
        }
        //At the end we use fixRate to fix the outgoing rate of all vertices
        fixRate(ret);
        return ret;
    }

    public static void main(String[] args) throws IOException {
        int n;//number of vertices
        int l;//number of chains to generate
        long startTime;
        long stopTime;
        long elapsedTime;
        Scanner keyboard = new Scanner(System.in);
        NumberFormat formatter = new DecimalFormat("#0.0000000000000000");
        //This file will be the imput file for VerifiRhoReversibility
        BufferedWriter out = new BufferedWriter(new FileWriter("inputRhoReversible.txt"));
        System.out.print("Insert the number of vertices (the number will be increased by 1): ");
        n = keyboard.nextInt();
        System.out.print("Insert the number of chains to generate: ");
        l = keyboard.nextInt();
        System.out.println("Generation of " + l + " chains each composed by " + (n + 1) + " vertices.");
        startTime = System.currentTimeMillis();
        //Write the number of chains and vertices in the output file
        out.write(l + "");
        out.newLine();
        out.write((n + 1) + "");
        out.newLine();
        for (int k = 0; k < l; k++) {
            Triple chain = getChain(n);//all chains have the same number of vertices
            stopTime = System.currentTimeMillis();
            elapsedTime = stopTime - startTime;
            System.out.println("Elapsed time: " + elapsedTime + "ms");
            double[][] edges = converter(chain.chain);
            double[] vertices = chain.pi;
            for (int i = 0; i < n + 1; i++) {
                System.out.print(vertices[i] + " ");
            }
            System.out.println("");
            out.write(chain.rho[0] + "");
            System.out.print(0 + "->" + chain.rho[0] + "/");
            for (int i = 1; i < n + 1; i++) {
                out.write("," + chain.rho[i]);
                System.out.print(i + "->" + chain.rho[i] + "/");
            }
            out.newLine();
            System.out.println("");
            System.out.println("");
            for (int i = 0; i < n + 1; i++) {
                System.out.print(formatter.format(edges[i][0]) + " | ");
                out.write(edges[i][0] + "");
                for (int j = 1; j < n + 1; j++) {
                    System.out.print(formatter.format(edges[i][j]) + " | ");
                    out.write("," + edges[i][j]);
                }
                System.out.println("");
                out.newLine();
            }
            System.out.println("---------------------------------------------");
        }
        out.close();
        stopTime = System.currentTimeMillis();
        elapsedTime = stopTime - startTime;
        System.out.println("Elapsed time: " + elapsedTime + "ms");
    }

    private static void rhoBalanceEquation(int a, int b, double[] pi, double[][] chain, int[] ro) {
        Random gen = new Random();
        chain[a][b] = gen.nextDouble();
        chain[ro[b]][ro[a]] = pi[a] * chain[a][b] / pi[b];
    }

    //fixRate fixes the outgoing rate of all vertices in the chain, when it finds a vertex with different outgoing rate than
    //his renaming vertex it will fix the difference generating an edge between the found vertex and the support node and another edge from
    //the support node to the renaming vertex
    private static void fixRate(Triple ret) {
        ArrayList<Integer> vertices = new ArrayList<>();
        Random gen = new Random();
        //we use this value to check if fixRate creates an edge to connect the additional vertex
        boolean flag = false;
        for (int i = 0; i < ret.chain.length; i++) {
            vertices.add(i);
        }
        while (!vertices.isEmpty()) {
            int vertex = vertices.remove((int) 0);
            if (ret.rho[vertex] != vertex) {
                vertices.remove((Integer) ret.rho[vertex]);
                double sum1 = outgoingRate(ret.chain, vertex);
                double sum2 = outgoingRate(ret.chain, ret.rho[vertex]);
                double edgeValue;
                if (sum1 > sum2) {
                    flag = true;
                    edgeValue = sum1 - sum2;
                    ret.chain[ret.rho[vertex]][ret.chain.length - 1] = edgeValue;
                    ret.chain[ret.chain.length - 1][vertex] = ret.pi[ret.rho[vertex]] * edgeValue / ret.pi[ret.chain.length - 1];
                } else {
                    flag = true;
                    if (sum2 > sum1) {
                        edgeValue = sum2 - sum1;
                        ret.chain[vertex][ret.chain.length - 1] = edgeValue;
                        ret.chain[ret.chain.length - 1][ret.rho[vertex]] = ret.pi[vertex] * edgeValue / ret.pi[ret.chain.length - 1];
                    }
                }
            }
        }
        //if there are no edges that connect the additional vertex we create one
        if (!flag) {
            int x = gen.nextInt(ret.chain.length - 1);
            rhoBalanceEquation(ret.chain.length - 1, x, ret.pi, ret.chain, ret.rho);
            ret.chain[ret.chain.length - 1][ret.rho[x]] = ret.chain[ret.chain.length - 1][x];
            ret.chain[ret.rho[x]][ret.chain.length - 1] = ret.chain[x][ret.chain.length - 1];
        }
    }

    //Returns the outgoing rate of a given vertex
    private static double outgoingRate(double[][] chain, Integer x) {
        double ret = 0;
        for (int j = 0; j < chain.length; j++) {
            ret += chain[x][j];
        }
        return ret;
    }

    public static double[][] converter(double chain[][]) {
        int n = chain.length;
        double max = 0.0;
        double loop;
        double sum;
        for (int i = 0; i < n; i++) {
            sum = 0.0;
            for (int j = 0; j < n; j++) {
                sum += chain[i][j];
            }
            if (sum >= max) {
                max = sum;
            }
        }
        for (int i = 0; i < n; i++) {
            loop = 1.0;
            for (int j = 0; j < n; j++) {
                chain[i][j] /= max;
                loop -= chain[i][j];
            }
            if (loop > 0.0000000001) {
                chain[i][i] = loop;//loop used to make te outgoing rates of the current vertex sum to 1
            }
        }
        return chain;
    }

}
