/*
 * Questo programma genera catene che come rinomine hanno solo involuzioni o rinomine in se stessi
 */
package Involuzioni;

import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author Matteo and Giuly
 */
public class Involuzioni {

    /*
     Questa funzione serve per creare la tripla ret contenente la matrice degli atchi, le pi e le ro.
     */
    public static Tripla getChain(int n) {
        Tripla ret = new Tripla();
        //generiamo n nodi, e poi 1 in piú per sistemare le rate uscenti
        ret.chain = new double[n + 1][n + 1];
        ret.pi = new double[n + 1];
        ret.ro = new int[n + 1];
        // s é l'insieme che contiene tutti i nodi che formano tra loro una componente connessa
        ArrayList<Integer> s = new ArrayList();
        //u é l'insieme dei nodi che non si trovano nella componenete connessa
        ArrayList<Integer> u = new ArrayList();
        ArrayList<Integer> appoggio = new ArrayList();
        Random gen = new Random();
        int a, b;
        
        for (int i = 0; i < n; i++) {
            appoggio.add(i);
        }
        //qui creo le ro come involuzioni. O da 2 o da 1. Per tutti i nodi n del grafo ma non per l'n+1 che aggiungo per sistemare
        //le rate perché quello deve essere per forza rinominato in se stesso
        while (!appoggio.isEmpty()){
            int temp = appoggio.get(gen.nextInt(appoggio.size()));
            ret.ro[temp]=appoggio.get(gen.nextInt(appoggio.size()));
            ret.ro[ret.ro[temp]] = temp;
            appoggio.remove((Integer) ret.ro[temp]);
            appoggio.remove((Integer) temp);
        }
        //rinomino il nodo aggiunto inse stesso
        ret.ro[n]=n;
        //generazione pi greco per tutti i nodi e li inserisco in u (tranne n+1)
        for (int i = 0; i < n; i++) {
            if (ret.pi[i] == 0) {
                ret.pi[i] = gen.nextDouble();
            }
            ret.pi[ret.ro[i]] = ret.pi[i];
            u.add(i);
        }
        //genero anche la pi del nodo fittizio
        ret.pi[n] = gen.nextDouble();
        //aggiungo un nodo all'insieme iniziale
        s.add(u.remove(gen.nextInt(u.size())));
        //generazione degli archi: finché ci sono nodi in u prendo un nodo da s, ne rimuovo uno da u e faccio i controlli per vedere se
        //fa giá parte o no della cc
        while (!u.isEmpty()) {
            a = s.get(gen.nextInt(s.size()));
            b = u.remove(gen.nextInt(u.size()));
            //se non c'é l'arco da a fino a b lo creo. Non devo controllare che non ci sia giá un altro arco da un nodo di s che va verso
            //u perché nel momento in cui lo metto, per definizione del mio programma, connetto tutto il nodo nella componente connessa
            if (ret.chain[a][b] == 0) {
                rinomine(a, b, ret.pi, ret.chain, ret.ro);
            }
            //poi invece devo controllare se c'é giá un arco dal nodo di u ad uno qualsiasi di quelli di s, perché potrebbe essere che 
            //la generazione degli archi con le rinomine mi abbia creato un arco dal mio nodo ad uno di quelli in s.
            boolean flag = false;
            int l = 0;
            while (!flag && l < s.size()) {
                int x = s.get(l);
                if (ret.chain[b][x] != 0) {
                    flag = true;
                }
                l=l+1;
            }
            if (!flag) {
                rinomine(b, s.get(gen.nextInt(s.size())), ret.pi, ret.chain, ret.ro);
            }
            //
            s.add(b);
        }
        sistemaRate(ret, gen);
        return ret;
    }

    public static void main(String[] args) {
        // TODO code application logic here
        int n = 6;
        long startTime = System.currentTimeMillis();
        long stopTime;
        long elapsedTime;
        Tripla tests[] = new Tripla[1];
        for (int k = 0; k < 1; k++) {
            Tripla chain = getChain(n);
            double archi[][] = chain.chain;
            double nodi[] = chain.pi;
            tests[k] = chain;
            for (int i = 0; i < n + 1; i++) {
                System.out.print(nodi[i] + " ");
            }
            System.out.println("");
            for (int i = 0; i < n + 1; i++) {
                System.out.print(i + "->" + chain.ro[i] + "/");
            }
            System.out.println("");
            System.out.println("");
            for (int i = 0; i < n + 1; i++) {
                for (int j = 0; j < n + 1; j++) {
                    System.out.print(archi[i][j] + " | ");
                }
                System.out.println("");
            }
            System.out.println("---------------------------------------------");
        }
        stopTime = System.currentTimeMillis();
        elapsedTime = stopTime - startTime;
        System.out.println("Elapsed time: " + elapsedTime + "ms");
    }

    private static void rinomine(int a, int b, double[] pi, double[][] chain, int[] ro) {
        Random gen = new Random();
        int aR = ro[b];
        int bR = ro[a];
        int temp;
        chain[a][b] = gen.nextDouble();
        chain[aR][bR] = pi[a] * chain[a][b] / pi[b];
        temp = aR;
        aR = ro[bR];
        bR = ro[temp];
        while (a != aR || b != bR) {
            chain[aR][bR] = gen.nextDouble();//forse funziona solo perche sono solo involuzioni
            chain[ro[bR]][ro[aR]] = pi[aR] * chain[aR][bR] / pi[bR];
            aR = ro[aR];
            bR = ro[bR];
        }
    }

    //trova il gruppo di rinomine associate a n
    public static ArrayList<Integer> findGroup(int[] ro, int n, ArrayList<Integer> nodi) {
        ArrayList<Integer> gruppo;
        int r = ro[n];
        int nIniz = n;
        int rIniz = r;
        gruppo = new ArrayList<>();
        do {
            System.out.println(n + " " + r);
            gruppo.add(n);
            nodi.remove((Integer) n);//n è l'etichetta, voglio togliere il nodo chiamato n non quello alla posizione n
            n = r;
            r = ro[r];
        } while (n != nIniz && r != rIniz);
        return gruppo;
    }

    private static double findMax(ArrayList<Integer> gruppo, double[][] chain, int n) {
        double temp = 0;
        double valMax = -1.0;
        for (Integer nodo : gruppo) {
            for (int j = 0; j < n; j++) {
                temp += chain[nodo][j];
            }
            if (temp > valMax) {
                valMax = temp;
            }
            temp = 0.0;
        }
        return valMax;
    }

    private static void sistemaRate(Tripla ret, Random gen) {
        ArrayList<Integer> gruppo;
        ArrayList<Integer> nodi = new ArrayList<>();
        for (int i = 0; i < ret.chain.length; i++) {
            nodi.add(i);
        }
        while (!nodi.isEmpty()) {
            int nodo = nodi.remove((int) 0);
            gruppo = findGroup(ret.ro, nodo, nodi);
            System.out.println("dimensioni = " + gruppo.size());
            double valMax = findMax(gruppo, ret.chain, ret.chain.length);//valore
            System.out.println(valMax);
            for (Integer x : gruppo) {
                double sommaUscenti = trovaSommaUscenti(ret.chain, x);
                System.out.println("sono uscenti");
                double valArco = valMax - sommaUscenti;
                System.out.println(sommaUscenti);
                if (valArco != 0.0) {
                    ret.chain[x][ret.chain.length - 1] = valArco;
                    ret.chain[ret.chain.length - 1][ret.ro[x]] = ret.pi[x] * valArco / ret.pi[ret.chain.length - 1];
                }
            }
        }
    }

    private static double trovaSommaUscenti(double[][] chain, Integer x) {
        double ret = 0;
        for (int j = 0; j < chain.length; j++) {
            ret += chain[x][j];
        }
        return ret;
    }
}
class Tripla {

    double pi[];
    double chain[][];
    int ro[];
}
