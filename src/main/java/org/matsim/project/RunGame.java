package org.matsim.project;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.swing.JFrame;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.facilities.ActivityFacilities;
import org.matsim.facilities.ActivityFacilitiesFactory;
import org.matsim.facilities.ActivityFacility;
import org.matsim.facilities.ActivityOption;
import org.matsim.facilities.ActivityOptionImpl;
  
public class RunGame extends Canvas{  
    static ArrayList<ActivityFacility> facilities = new ArrayList<>();
    static ArrayList<Link> links = new ArrayList<>();
    static Coord lstart = new Coord();
    static Network net = null;
    static NetworkFactory netf = null;
    static int next_node = 0;
    
    static ActivityFacilitiesFactory ff = null;
    
    static String mode = "links";
    static ArrayList<String> fac_modes = new ArrayList<>(Arrays.asList("residential","work","services","education","leisure"));
    static HashMap<String,Color> fac_colors = new HashMap<>();
    static String fac_mode = "residential";
    static int fac_size = 0;
    
    public void initfacs() {
    	final ActivityFacilities facilities = ScenarioUtils.createScenario(ConfigUtils.createConfig()).getActivityFacilities() ;
		ff = facilities.getFactory();
		
		fac_colors.put("residential", Color.RED);
		fac_colors.put("work", Color.GRAY);
		fac_colors.put("education", Color.ORANGE);
		fac_colors.put("leisure", Color.GREEN);
		fac_colors.put("services", Color.MAGENTA);

    }
    public void createNet() {
    	// create an empty network
    			net = NetworkUtils.createNetwork();
    			netf = net.getFactory();
    			
    }
    
	private static void setLinkAttributes(Link link, double capacity, double length, double travelTime) {
		link.setCapacity(capacity);
		link.setLength(length);
		// agents have to reach the end of the link before the time step ends to
		// be able to travel forward in the next time step (matsim time step logic)
		link.setFreespeed(link.getLength() / (travelTime - 0.1));
	}
	
	public static Node checkClosestNode(Coord c) {
		Node nnode = NetworkUtils.getNearestNode(net, c);
		if (nnode == null) return null;
		Coord cnear = nnode.getCoord();
		if (CoordUtils.calcEuclideanDistance(c, cnear)<20) {
			return nnode;
		}
		else {
			return null;
		}
	}
	
	public static Node getClosestNodeOrMakeNew(Coord coord_click) {
		Node cn0 = checkClosestNode(coord_click);
		Node n0 = null;
    	if (cn0==null) {
        	n0 = netf.createNode(Id.createNodeId(next_node), new Coord(coord_click.getX(), coord_click.getY()));
    		net.addNode(n0);
    		next_node++;
    	}
    	else {
    		n0 = cn0;
    	}
    	return n0;

	}
    public static Link createLink(Coord lend) {
    	Node n0 = getClosestNodeOrMakeNew(lstart);
		Node n1 = getClosestNodeOrMakeNew(lend);
		
		Link l = netf.createLink(Id.createLinkId(n0.getId().toString()+"_"+n1.getId().toString()), n0, n1);
		setLinkAttributes(l, 3000, 100, 100);
		net.addLink(l);
		
		return l;
    }
    public void paint(Graphics g) {  
        g.drawString("Mode: "+mode+" Fac_type: "+fac_mode+" Fac_size: "+fac_size+""
        			+ "\nLinks: "+String.valueOf(links.size())+" Nodes: " 
        			+String.valueOf(net.getNodes().size()),40,40);  
        //setBackground(Color.WHITE);  
        //g.fillRect(130, 30,100, 80);  
        //g.drawOval(30,130,50, 60);  
        setForeground(Color.BLACK);  
        //g.fillOval(130,130,50, 60);  
        //g.drawArc(30, 200, 40,50,90,60);  
        //g.fillArc(30, 130, 40,50,180,40);  
        //g.drawLine(0, 0, 100, 100);
        for (ActivityFacility af:facilities) {
        	g.setColor(fac_colors.get(af.getActivityOptions().values().iterator().next().getType()));
        	Coord c = af.getCoord();
        	//g.fillArc((int) c.getX(), (int) c.getY(), 10, 10, 0, 360);
        	int cx = (int) c.getX();
        	int cy = (int) c.getY();
        	g.drawPolygon(new int[] {cx-7, cx+7, cx}, new int[] {cy+4,cy+4,cy-8}, 3); //triangle
        }
        g.setColor(Color.black);
        for (Link l:links) {
        	Coord fn = l.getFromNode().getCoord();
        	Coord tn = l.getToNode().getCoord();
        	
        	g.drawLine((int)fn.getX(), (int)fn.getY(), (int)tn.getX(), (int)tn.getY());
        	//System.out.println(fn.toString()+"_"+tn.toString());
        }
        
        for (Node n:net.getNodes().values()) {
        	g.fillArc((int) n.getCoord().getX()-5, (int) n.getCoord().getY()-5, 10, 10, 0, 360);
        }
          
    }  
    
    public static void main(String[] args) {  
        RunGame m=new RunGame();  
        m.createNet();
        m.initfacs();
        JFrame f=new JFrame(); 
        
        m.addKeyListener(new KeyAdapter() {
        	public void keyPressed(KeyEvent e){
        		if (Character.compare(e.getKeyChar(), 'm')==0) {
        			if (mode.equals("links")) mode= "facilities";
        			else if (mode.equals("facilities")) mode = "links";
        			
        		}
        		if (Character.compare(e.getKeyChar(), 'w')==0) {
        			fac_size++;
        		}
        		if (Character.compare(e.getKeyChar(), 's')==0) {
        			fac_size--;
        			if(fac_size<0) fac_size = 0;
        		}
        		if (Character.compare(e.getKeyChar(), 'd')==0) {
        			int index = fac_modes.indexOf(fac_mode);
        			if (index == fac_modes.size()-1) index = -1;
        			fac_mode = fac_modes.get(index+1);
        		}
        		if (Character.compare(e.getKeyChar(), 'a')==0) {
        			int index = fac_modes.indexOf(fac_mode);
        			if (index == 0) index = fac_modes.size();
        			fac_mode = fac_modes.get(index-1);
        		}
        		m.repaint();
        	}
        });
        m.addMouseListener(new MouseAdapter() {
        	@Override
            public void mousePressed(MouseEvent e){
            	if (mode.equals("facilities")) {
            		ActivityFacility af =  ff.createActivityFacility(Id.create(0, ActivityFacility.class), new Coord(e.getX(), e.getY())) ; 
            		facilities.add(af);
            		ActivityOption ao = new ActivityOptionImpl(fac_mode);
            		ao.setCapacity(fac_size);
            		af.getActivityOptions().put(fac_mode, ao);
            	}
            	else {
                    lstart = new Coord(e.getX(), e.getY());
            	}
                System.out.println("Clicked");
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            	if (mode.equals("links")) {
                	links.add(createLink(new Coord(e.getX(), e.getY())));
                    
            	}
            	System.out.println("mouseReleased");
                m.repaint();

            }
        });
        
        //System.out.println(f.getMouseListeners());
        
        f.add(m);  
        f.setSize(800,800);  
        //f.setLayout(null);  
        f.setVisible(true);  
    }  
  
}  
