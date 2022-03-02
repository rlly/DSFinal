import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.io.*;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import java.lang.Object;
import java.lang.Math;
import java.lang.Thread;

import java.text.SimpleDateFormat;
import javax.swing.text.JTextComponent;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;



public class FinalProject extends JFrame implements ActionListener{
    
    // Bunch of Structures to hold Data
    public static ArrayList<Integer> verts = new ArrayList<Integer>(); //list of vertices
    public static HashMap<Integer, ArrayList<Integer>> vertCoords = new HashMap<Integer, ArrayList<Integer>>(); //(vertex, xy coordinate)
    public static ArrayList<ArrayList<Integer>> edges = new ArrayList<ArrayList<Integer>>(); //list of edges ((v1v2),(v1v3),(v2v3)...)
    public static HashMap<String, Integer> weights = new HashMap<String, Integer>(); //(("v1v2"), (random weight))
    public static ArrayList<String> fileRead = new ArrayList<String>(); //list of strings read from graph file
    public static HashMap<Integer, Boolean> vertical = new HashMap<Integer, Boolean>(); //(+/-speed, t/f) up, down bounce
    public static HashMap<Integer, Boolean> horizontal = new HashMap<Integer, Boolean>(); //(+/-speed, t/f) left, right bounce
    public static HashMap<Integer, Integer> speed = new HashMap<Integer, Integer>(); //assigns speeds to vertices((v1, speed),(v1, speed),...)
    public static ArrayList<ArrayList<Integer>> MWSTEdges = new ArrayList<ArrayList<Integer>>(); // list of edges in MWST((v1v2),(v1v3),(v2v3)...)
    public static ArrayList<ArrayList<Integer>> MWSTEdgesClick = new ArrayList<ArrayList<Integer>>(); //list of pairs of clicked verts
    public static HashMap<String, Boolean> treeCheck = new HashMap<String, Boolean>(); //(v1v2, true) - list of visited edges when finding mwst edges
    public static HashMap<String, Boolean> clickCheck = new HashMap<String, Boolean>(); //(v1v2, true) - list of clicked edges when clicking mwst edges
    public static ArrayList<Integer> clickedVerts = new ArrayList<Integer>(); //PAIR of clicked vertices -> clicked edge
    public static HashMap<Integer, Boolean> boolVert = new HashMap<Integer, Boolean>(); //(v1, true) - list of clicked vertices
    
    public static boolean gameDone = false;
    public static boolean colorFlash = false;

    public static boolean exitTimer = false;
    
    //mouse stuff
    MouseHandler mouseEvt = new MouseHandler(this);
    public static boolean down = false;
    public static boolean up = false;
    public static int xOffset = 10; //offsets from top left corner of where circle is drawn (closer to center of circle)
    public static int yOffset = 15;


    // Creates coordinates for each vertex, stored in vertexCoords Hashmap
    public static ArrayList<Integer> createCoords(){
        ArrayList<Integer> coords = new ArrayList<Integer>();
        coords.add((int)Math.floor(Math.random()*800)); //random x
        coords.add((int)Math.floor(Math.random()*600)); //random y
        return coords;
    }
    
    // Moves a vertex horizontally and vertically
    // depending on its speed. If vertex touches a wall,
    // bounces off wall by reversing direction.
    //
    public static ArrayList<Integer> moveCoords(int vert, int x, int y){
        ArrayList<Integer> coords = new ArrayList<Integer>();
        if( x < 2 ){
            horizontal.put(vert, false);
        }
        if( x > 770 ){
            horizontal.put(vert, true);
        }
        if( y < 2 ){
            vertical.put(vert, false);
        }
        if( y > 570 ){
            vertical.put(vert, true);
        }
        
        if( horizontal.get(vert) ){
            coords.add(x-speed.get(vert));
        }
        else coords.add(x+speed.get(vert));
        if( vertical.get(vert) ){
            coords.add(y-speed.get(vert));
        }
        else coords.add(y+speed.get(vert));;
        return coords;
    }
    
    // Creates an edge to put in edges ArrayList and
    // maps edge and its reverse to a weight in weights Hashmap
    // Example:
    // edge = (0,1);
    // [(0,1), Weight] and [(1,0), Weight]
    // are put into the Hashmap.
    public static ArrayList<Integer> createEdge(int a, int b){
        ArrayList<Integer> edge = new ArrayList<Integer>();
        edge.add(a);
        edge.add(b);
        int weight = (int)(1+Math.floor(Math.random()*14));
        weights.put(String.valueOf(a)+String.valueOf(b), weight);
        weights.put(String.valueOf(b)+String.valueOf(a), weight);
        return edge;
    }

    // Creates an edge (v1v2) to put in MWSTEdges ArrayList
    public static ArrayList<Integer> createMWSTEdge(int a, int b){
        ArrayList<Integer> edge = new ArrayList<Integer>();
        edge.add(a);
        edge.add(b);
        return edge;
    }

    // generates graph and
    // makes text file for graph (edge and vertices only)
    public static void generate(int V){
        //int V = 4;
        //int E = ((V-1)*(V-2))/2;
        int E = (V*(V-1))/2;
        //int E = V-1;
        double p = 0.25;
        int V1 = V/2;
        int V2 = V - V1;
        
        // Test
        for(int i = 0; i < V; i++){
            verts.add(i);
            horizontal.put(i,true);
            vertical.put(i,true);
            speed.put(i, (int) (Math.floor(Math.random()*4)+2)); //(vertex, assigned speed)
        }
        for(int i = 0; i < verts.size(); i++){
            vertCoords.put(i,createCoords()); //((vertex, assigned coordinate),...)
        }
            

        //making text file in folder
        Graph printGraph = simple(V, E);
        PrintStream terminal = System.out;
        try{
            PrintStream graphtxt = new PrintStream(new File("Graph.txt"));
            System.setOut(graphtxt);
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }
        StdOut.println(printGraph);
        System.setOut(terminal); //back to terminal, not folder

        File graph = new File("Graph.txt");
        try {
            Scanner sc = new Scanner(graph);
            sc.nextLine();
            while (sc.hasNext()) {
                String i = sc.next();
                //System.out.println(i);
                fileRead.add(i);
            }
            sc.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        int linkVert1 = 0;
        int linkVert2 = 0;
        for(int i = 0; i < fileRead.size(); i++){
            if(fileRead.get(i).endsWith(":")){
                String num = fileRead.get(i);
                num = num.substring(0,num.length()-1);
                linkVert1 = Integer.valueOf(num);
            }
            else{
                String num = fileRead.get(i);
                linkVert2 = Integer.valueOf(num);
                if(!weights.containsKey(String.valueOf(linkVert1)+String.valueOf(linkVert2))){
                    edges.add(createEdge(linkVert1,linkVert2));
                }
            }
        }
    }

    //makes text file for graph + weights
    //v1 v2 weight
    public void gWeights(){
        PrintStream terminal = System.out;
        try{
            PrintStream graphWtxt = new PrintStream(new File("GraphW.txt"));
            System.setOut(graphWtxt);
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }
        System.out.println(edges.size());
        for(int i = 0; i < edges.size(); i++){
            int edgev1 = edges.get(i).get(0);
            int edgev2 = edges.get(i).get(1);
            System.out.print(edgev1+" "+edgev2+" "+weights.get(String.valueOf(edgev1)+String.valueOf(edgev2)));
            System.out.println();
        }
        System.setOut(terminal);
    }

    //reads file made from gWeights (edges with weights) and
    //makes new text file with the found MWST
    public void gMWST(){
        PrintStream terminal = System.out;
        try{
            PrintStream graphMWSTtxt = new PrintStream(new File("GraphMWST.txt"));
            System.setOut(graphMWSTtxt);
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }
        DSGraph G = new DSGraph();
        G.readGraph("GraphW.txt");
        System.out.println(G.MWST());
        System.setOut(terminal);
    }

    //read file made from gMWST and
    //makes "key" arrayList of correct edges (no weights)
    public void readMWSTEdges(){
        fileRead.clear();
        File graph = new File("GraphMWST.txt");
        try {
            Scanner sc = new Scanner(graph);
            sc.nextLine();
            while (sc.hasNext()) {
                String i = sc.next();
                //System.out.println(i);
                fileRead.add(i);
            }
            sc.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        int linkVert1 = 0;
        int linkVert2 = 0;
        for(int i = 0; i < fileRead.size(); i++){
            if(fileRead.get(i).endsWith(":")){
                String num = fileRead.get(i);
                num = num.substring(0,num.length()-1);
                linkVert1 = Integer.valueOf(num);
            }
            else{
                String num = fileRead.get(i);
                linkVert2 = Integer.valueOf(num);
                if(!treeCheck.containsKey(String.valueOf(linkVert1)+String.valueOf(linkVert2))){
                    MWSTEdges.add(createMWSTEdge(linkVert1,linkVert2));
                    treeCheck.put(String.valueOf(linkVert1)+String.valueOf(linkVert2), true); //adds edges to hashmap of visited edges
                    treeCheck.put(String.valueOf(linkVert2)+String.valueOf(linkVert1), true);
                }
            }
        }
    }
    
    //clears data when player chooses "back" buttons
    public static void clear(){
        verts.clear();
        vertCoords.clear();
        edges.clear();
        weights.clear();
        fileRead.clear();
        vertical.clear();
        horizontal.clear();
        speed.clear();
        MWSTEdges.clear();
        MWSTEdgesClick.clear();
        treeCheck.clear();
        clickCheck.clear();
        clickedVerts.clear();
        boolVert.clear();
    }
    
    JFrame MWST;
    
    //main menu
    JPanel ChooseLevel;
    JButton B1;
    JButton B2;
    JButton B3;
    
    //Start screen
    JPanel S;
    JButton S1;
    JButton Back;
    
    //game screen
    int Level;
    JPanel Game;
    JLabel Timer;
    JPanel Clock;
    JPanel Graph;
    JButton Back2;
    
    //leaderboard
    JPanel LeadBoard;
    JLabel Top;
    JLabel Name;
    JLabel Time;
    JLabel first;
    JLabel second;
    JLabel third;
    JLabel fourth;
    JLabel fifth;
    
    //constants; board dimensions
    int Width = 875;
    int Height = 700;
    
    // timer
    long startTime;
    
    public FinalProject(){
        MWST = new JFrame("Find the MWST");
        
        
        //main menu screen
        ChooseLevel = new JPanel();
        ChooseLevel.setPreferredSize(new Dimension(800, 600));
        ChooseLevel.setLayout(new GridLayout(9, 9));
        //buttons for main menu screen
        B1 = new JButton("Level 1");
        B1.setPreferredSize(new Dimension (50, 40));
        B2 = new JButton("Level 2");
        B2.setPreferredSize(new Dimension (50, 40));
        B3 = new JButton("Level 3");
        B3.setPreferredSize(new Dimension (50, 40));
        
        //2nd menu screen
        //top of start screen
        S = new JPanel();
        S1 = new JButton("START");
        Back = new JButton("Back");
        Color LPurple = new Color(243, 225, 255);
        
        
        //game screen
        Game = new JPanel();
        Clock = new JPanel();
        Timer = new JLabel();
        Back2 = new JButton("Back");
        

        Graph = new JPanel(){
            @Override
            public void paintComponent(Graphics g) {
                
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                //draws circle vertices at xy coordinates of each vertex
                for(int i = 0; i < verts.size(); i++){
                    g2.setColor(Color.BLUE);
                    g2.drawOval(vertCoords.get(i).get(0),vertCoords.get(i).get(1),25,25);
                    /****commented out code drew labels on vertices
                    //g2.setColor(Color.BLACK);
                    //g2.drawString(Integer.toString(i),vertCoords.get(i).get(0)+10,vertCoords.get(i).get(1)+15);
                    //System.out.println(i+ ": "+vertCoords.get(i).get(0) +","+vertCoords.get(i).get(1));
                    */
                }
                
                
                //draws weight labels on edges
                //random looking "+10" and "+15" offsets coordinates from top left corner of circle
                //draws edges connecting vertices
                for(int i = 0; i < edges.size(); i++){
                    g2.setColor(Color.BLACK);
                    setFont(new Font("TimesRoman", Font.BOLD, 14));
                    g2.drawString(Integer.toString(weights.get(String.valueOf(edges.get(i).get(0))+
                                                                String.valueOf(edges.get(i).get(1)))),
                                  (vertCoords.get(edges.get(i).get(0)).get(0)+10+vertCoords.get(edges.get(i).get(1)).get(0)+10)/2,
                                  (vertCoords.get(edges.get(i).get(0)).get(1)+15+vertCoords.get(edges.get(i).get(1)).get(1)+15)/2);
                    /*System.out.println("Edge: "+String.valueOf(edges.get(i).get(0))+String.valueOf(edges.get(i).get(1))+
                                       ", Weight: "+weights.get(String.valueOf(edges.get(i).get(0))+
                                                                String.valueOf(edges.get(i).get(1))));*/
                    g2.setColor(Color.BLUE);
                    g2.setStroke(new BasicStroke(1));
                    g2.drawLine(vertCoords.get(edges.get(i).get(0)).get(0)+10,vertCoords.get(edges.get(i).get(0)).get(1)+15,
                               vertCoords.get(edges.get(i).get(1)).get(0)+10,vertCoords.get(edges.get(i).get(1)).get(1)+15);
                }
               
                //when correct edges are clicked (are added to list of clicked edges) edge changes color
                for(int i = 0; i < MWSTEdgesClick.size(); i++){
                    g2.setColor(Color.RED);
                    g2.drawLine(vertCoords.get(MWSTEdgesClick.get(i).get(0)).get(0)+10,vertCoords.get(MWSTEdgesClick.get(i).get(0)).get(1)+15,
                               vertCoords.get(MWSTEdgesClick.get(i).get(1)).get(0)+10,vertCoords.get(MWSTEdgesClick.get(i).get(1)).get(1)+15);
                }
                //when correct vertices are clicked (are added to list of clicked vertices) vertex changes color
                for(int i = 0; i < clickedVerts.size(); i++){
                    g2.setColor(Color.RED);
                    g2.drawOval(vertCoords.get(clickedVerts.get(i)).get(0),vertCoords.get(clickedVerts.get(i)).get(1),25,25);
                }
                //if mouseclick, then indicate click location with pink circle
                if(mouseEvt.pressStat){
                    g2.setColor(Color.PINK);
                    g2.drawOval(mouseEvt.x-xOffset,mouseEvt.y-yOffset,25,25);
                }
                //when all mwstedges are clicked (are all added to clicked mwst edges) game ends
                if(MWSTEdges.size() == MWSTEdgesClick.size()){
                    if(colorFlash)
                        g2.setColor(Color.BLUE);
                    else g2.setColor(Color.RED);
                    g2.drawString("MWST Complete", 500, 500);
                    gameDone = true; //ends game when player finds complete mwst
                }
                
                repaint();
            }
        };
        Graph.addMouseListener(mouseEvt);
        Graph.addMouseMotionListener(mouseEvt);
            
        //end of game screen
        LeadBoard = new JPanel();
        Top = new JLabel("Top Five Times");
        Name = new JLabel("Name");
        Time = new JLabel("Time");
        first = new JLabel("1");
        second = new JLabel("2");
        third = new JLabel("3");
        fourth = new JLabel("4");
        fifth = new JLabel("5");
        
        MWST.setSize(Width, Height);
        //click x to close the window
        MWST.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //add panels to level screen and start screen
        MWST.add(ChooseLevel);
        
        //set menu colors
        Color ChooseLevelBGC = new Color(213, 173, 245);
        ChooseLevel.setBackground(ChooseLevelBGC);
        //adds buttons to main menu panel
        ChooseLevel.add(B1);
        ChooseLevel.add(B2);
        ChooseLevel.add(B3);
        B1.addActionListener(this);
        B2.addActionListener(this);
        B3.addActionListener(this);
        
        //set start screen
        Color StartBGC = new Color(116, 178, 94);
        S.setBackground(StartBGC);
        S.add(S1);
        S.add(Back);
        S1.addActionListener(this);
        Back.addActionListener(this);
        
        //set game screen
        Color Teel = new Color(77, 209, 209);
        Game.setBackground(Teel);
        Color LPink = new Color(255, 225, 240);
        Clock.setBackground(LPink);
        Game.add(Clock);
        Clock.add(Timer);
        Color LYellow = new Color(254, 255, 206);
        Graph.setBackground(LYellow);
        Game.add(Back2);
        Back2.addActionListener(this);
        
        MWST.setVisible(true);
    }

    // Check if a vertex has been clicked on
    // When 2 are clicked on, an edge is formed
    // and checked if in MWST. Resets # of
    // clicked vertices after check.
    public void checkVertClick(){
        //coordinates of first click
        int x1 = mouseEvt.x;
        int y1 = mouseEvt.y;
        for(int i = 0; i < verts.size(); i++){
            //gets coordinate of vertex (offset to make coordinate closer to center of the circle
            int x2 = vertCoords.get(i).get(0)+xOffset;
            int y2 = vertCoords.get(i).get(1)+yOffset;
            double dist = Math.sqrt( (double) ( ( Math.pow((double)(x2-x1),2) ) + ( Math.pow((double)(y2-y1),2) ) ) );
            if(dist < 13.5 && !boolVert.containsKey(i)){ //if click was within vert circle and not already clicked
                clickedVerts.add(i); //add 1st clicked coordinates to arraylist
                boolVert.put(i,true); //list of already clicked vertices
                break;
            }
        }
        if(clickedVerts.size() > 1){ //if first click coordinate is found
            //gets "name" of clicked verts
            String v1 = String.valueOf(clickedVerts.get(0));
            String v2 = String.valueOf(clickedVerts.get(1));
            
            String edge = v1+v2;
            String rEdge = v2+v1;
            //if first edge clicked was part of edges in MWST and not the same first edge, then
            if( treeCheck.containsKey(edge) && !clickCheck.containsKey(edge) ){
                //adds to clickCheck, so second coordinate can be taken
                clickCheck.put(edge, true);
                clickCheck.put(rEdge, true);
                MWSTEdgesClick.add(createMWSTEdge(Integer.valueOf(v1), Integer.valueOf(v2))); //adds clicked edge to a list of clicked MWST edges
                clickedVerts.clear(); //arraylist should only hold 2 items at a time ~ the current first vert that is clicked
                boolVert.clear(); //should only hold 2 items at a time; (1st clicked vertex, true), get 2nd vertex
            }
            //clears after finding or not finding edge pair
            else{
                clickedVerts.clear();
                boolVert.clear();
            }
        }
    }
    
    // Updates the screen
    public void update() {
        for(int i = 0; i < verts.size(); i++){
            int prevX = vertCoords.get(i).get(0);
            int prevY = vertCoords.get(i).get(1);
            vertCoords.put(i,moveCoords(i,prevX,prevY)); //vertex, moved(previous coordinates) ~ always contains current coordinates
        }
        if(mouseEvt.pressStat && !down){
            down = true;
        }
        if(!mouseEvt.pressStat && down){
            up = true;
        }
        if(down && up){
            checkVertClick();
            down = false;
            up = false;
        }
    }
    
    // https://stackoverflow.com/questions/44453825/how-do-i-correctly-restart-a-java-util-timer
    private void launchSomeTimer() {
        TimerTask timerTask = new TimerTask() {
            
            @Override
            public void run() {
                if(exitTimer){
                    cancel();
                }
                if(!gameDone){
                    long now = System.currentTimeMillis();
                    long gameTime = now - startTime;
                    Timer.setText("" + (TimeUnit.MILLISECONDS.toMinutes(gameTime))+ "min " + singleSec(TimeUnit.MILLISECONDS.toSeconds(gameTime)) + (TimeUnit.MILLISECONDS.toSeconds(gameTime)%60) + "sec"); //+ "." + singleMilli(gameTime) + (gameTime%(100)) + "sec");
                    update();
                }
                else{
                    if(colorFlash) colorFlash = false;
                    else colorFlash = true;
                }
            }
        };
        Timer timer = new Timer();
        timer.schedule(timerTask, 100, 100);
        
    }
    
    // Adds a "0" to single digit tenths of a second
    public static String singleMilli(long i){
        if( i%100 < 10){
            return "0";
        }
        else return "";
    }
    
    // Adds a "0" to single digit seconds
    public static String singleSec(long i){
        if( i%60 < 10){
            return "0";
        }
        else return "";
    }


    public void actionPerformed(ActionEvent e){
        Object source = e.getSource();
        if(source == B1){
            ChooseLevel.setVisible(false);
            MWST.add(S);
            S.setVisible(true);
            Level = 1;
                //goes to level one graph generator with start button
                //graph.setVisible(false) until start button is clicked
            generate(4); //generates graph with v=4
        }
        if(source == B2){
            ChooseLevel.setVisible(false);
            MWST.add(S);
            S.setVisible(true);
            Level = 2;
            generate(5); //generates graph with v=5
        }
        if(source == B3){
            ChooseLevel.setVisible(false);
            MWST.add(S);
            S.setVisible(true);
            Level = 3;
            generate(6); //generates graph with v=6
        }
        if(source == S1){
            exitTimer = false;
            S.setVisible(false);
            MWST.add(Game);
            Game.setVisible(true);
            startTime = System.currentTimeMillis();
            launchSomeTimer();
            Graph.setPreferredSize(new Dimension(800, 600));
            Game.add(Graph);
            gWeights();
            gMWST();
            readMWSTEdges();
            //System.out.println(MWSTEdges.size()+","+edges.size());
        }
        if(source == Back){
            S.setVisible(false);
            ChooseLevel.setVisible(true);
            clear();
            gameDone = false;
            exitTimer = true;
        }
        if(source == Back2){
            Game.setVisible(false);
            ChooseLevel.setVisible(true);
            clear();
            gameDone = false;
            exitTimer = true;
        }
    }
    
    //from https://algs4.cs.princeton.edu/41graph/GraphGenerator.java.html
    public class GraphGenerator {
        private final class Edge implements Comparable<Edge> {
            private int v;
            private int w;
            
            private Edge(int v, int w) {
                if (v < w) {
                    this.v = v;
                    this.w = w;
                }
                else {
                    this.v = w;
                    this.w = v;
                }
            }
            
            public int compareTo(Edge that) {
                if (this.v < that.v) return -1;
                if (this.v > that.v) return +1;
                if (this.w < that.w) return -1;
                if (this.w > that.w) return +1;
                return 0;
            }
        }
}
    
    
    /**
     * Returns a random simple graph containing {@code V} vertices and {@code E} edges.
     * @param V the number of vertices
     * @param E the number of vertices
     * @return a random simple graph on {@code V} vertices, containing a total
     *     of {@code E} edges
     * @throws IllegalArgumentException if no such simple graph exists
     */
    public static Graph simple(int V, int E) {
        if (E > (long) V*(V-1)/2) throw new IllegalArgumentException("Too many edges");
        if (E < 0)                throw new IllegalArgumentException("Too few edges");
        Graph G = new Graph(V);
        SET<String> set = new SET<String>();
        while (G.E() < E) {
            int v = StdRandom.uniform(V);
            int w = StdRandom.uniform(V);
            String e1 = String.valueOf(v)+String.valueOf(w);
            String e2 = String.valueOf(w)+String.valueOf(v);
            if ((v != w) && !set.contains(e1)) {
                set.add(e1);
                set.add(e2);
                G.addEdge(v, w);
            }
        }
        return G;
    }
    
    public static void main(String[] args){
        FinalProject finals = new FinalProject();
        }
}
