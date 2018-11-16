package stippling.main;

import java.awt.event.*;
import javax.swing.*;

//-----------------------------------------------------------------------------

/**
 * The app's main menu.
 */
public class MainMenu extends JMenuBar 
{
	/** ID to avoid warning. */
	private static final long serialVersionUID = 1L;
	
//	/** Reference to main frame. */
//	private Main mainFrame = null;
	
	/**
	 * Constructor.
	 * @param al main frame's action listener for handling menu actions.
	 * @param il main frame's item listener for handling menu updates.
	 */
	public MainMenu(ActionListener al, ItemListener il)
	{
		JMenuItem menuItem;
		JCheckBoxMenuItem cbMenuItem;
	    JRadioButtonMenuItem rbMenuItem;
	    ButtonGroup group;
	    JMenu subMenu;
	    
	    //------------------------------------
	    //	"File" menu
	    JMenu menu = new JMenu("File");
	    menu.setMnemonic(KeyEvent.VK_F);
	    this.add(menu);
	    
	    menuItem = new JMenuItem("Load Image");
	    menuItem.setMnemonic(KeyEvent.VK_L);
	    menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.META_MASK));
	    menuItem.addActionListener(al);
	    menu.add(menuItem);    

	    menu.addSeparator();

	    menuItem = new JMenuItem("Load Dots");
	    menuItem.setMnemonic(KeyEvent.VK_D);
	    menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.META_MASK));
	    menuItem.addActionListener(al);
	    menu.add(menuItem);    

	    menuItem = new JMenuItem("Save Dots");
	    menuItem.setMnemonic(KeyEvent.VK_S);
	    menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.META_MASK));
	    menuItem.addActionListener(al);
	    menu.add(menuItem);  
	    
	    menuItem = new JMenuItem("Save Mesh");
	    menuItem.setMnemonic(KeyEvent.VK_M);
	    menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.META_MASK));
	    menuItem.addActionListener(al);
	    menu.add(menuItem); 

	    menu.addSeparator();
	    
	    menuItem = new JMenuItem("Quit");
	    menuItem.setMnemonic(KeyEvent.VK_Q);
	    menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.META_MASK));
	    menuItem.addActionListener(al);
	    menu.add(menuItem);    
	   	    
	    //------------------------------------
	    //	"Stipple" menu
	    menu = new JMenu("Stipple");
	    menu.setMnemonic(KeyEvent.VK_S);
	    this.add(menu);

	    menuItem = new JMenuItem("Start");
	    menuItem.setMnemonic(KeyEvent.VK_S);
	    menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.META_MASK));
	    menuItem.addActionListener(al);
	    menu.add(menuItem);    

		cbMenuItem = new JCheckBoxMenuItem("Pause");
		menuItem.setMnemonic(KeyEvent.VK_U);
	    menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, ActionEvent.META_MASK));
	    cbMenuItem.setSelected(Stippler.pause);
	    cbMenuItem.addItemListener(il);
	    menu.add(cbMenuItem);    

	    menuItem = new JMenuItem("Stop");
	    menuItem.setMnemonic(KeyEvent.VK_T);
	    menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.META_MASK));
	    menuItem.addActionListener(al);
	    menu.add(menuItem);    

	    menu.addSeparator();

	    subMenu = new JMenu("Dots");
	    subMenu.setMnemonic(KeyEvent.VK_D);	    
	    group = new ButtonGroup();
	   
	    final int[] dots = { 100, 200, 300, 400, 500, 600, 700, 800, 900, 
	    		1000, 2000, 3000, 4000, 5000, 6000, 7000, 8000, 9000, 
	    		10000, 12000, 14000, 16000, 18000, 20000, 22000, 24000, 26000, 28000, 
	    		30000, 40000, 50000, 60000, 70000, 80000, 100000 };
	    for (int n = 0; n < dots.length; n++)
	    {
	    	final String label = dots[n] + " Dots";
	    	rbMenuItem = new JRadioButtonMenuItem(label);
	    	rbMenuItem.setSelected(Stippler.numDots == dots[n]);
	    	rbMenuItem.addActionListener(al);
	    	group.add(rbMenuItem);
	    	subMenu.add(rbMenuItem);
	    }
	    menu.add(subMenu);	    

	    //menu.addSeparator();
	    
	    subMenu = new JMenu("Passes");
	    subMenu.setMnemonic(KeyEvent.VK_P);	    
	    group = new ButtonGroup();
	   
	    final int[] passes = { 1, 2, 4, 8, 10, 15, 20, 25, 30, 40, 50, 60, 70, 80, 90, 100, 120, 140, 160, 180, 200 };
	    for (int n = 0; n < passes.length; n++)
	    {
	    	final String label = passes[n] + " Passes";
	    	rbMenuItem = new JRadioButtonMenuItem(label);
	    	rbMenuItem.setSelected(Stippler.numPasses == passes[n]);
	    	rbMenuItem.addActionListener(al);
	    	group.add(rbMenuItem);
	    	subMenu.add(rbMenuItem);
	    }
	    menu.add(subMenu);	    

	    menu.addSeparator();

	    subMenu = new JMenu("Dot Size");
	    subMenu.setMnemonic(KeyEvent.VK_M);	    
	    group = new ButtonGroup();
	    final double[] multiple = { 0.1, 0.2, 0.3, 0.4, 0.5, 
	    		0.5, 0.6, 0.7, 0.75, 0.8, 0.85, 0.9, 0.95, 1,
	    		1.05, 1.1, 1.15, 1.2, 1.25, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9, 
	    		2, 2.25, 2.5, 2.75, 3, 3.5, 4, 5, 6, 7, 8, 9, 10 };
	    for (int n = 0; n < multiple.length; n++)
	    {
	    	final String label = "Scale x " + multiple[n];  //String.format("%f", multiple[n]);
	    	rbMenuItem = new JRadioButtonMenuItem(label);
	    	rbMenuItem.setSelected(Stippler.dotSize == multiple[n]);
	    	rbMenuItem.addActionListener(al);
	    	group.add(rbMenuItem);
	    	subMenu.add(rbMenuItem);
	    }
	    menu.add(subMenu);	    

	    subMenu = new JMenu("Dot Range");
	    subMenu.setMnemonic(KeyEvent.VK_S);	    
	    group = new ButtonGroup();
	    final double[] range = { 0.0, 0.25, 0.5, 0.6, 0.7, 0.8, 0.9, 1, 1.1, 1.2, 
	    		1.3, 1.4, 1.5, 1.75, 2, 2.5, 3, 3.5, 4, 5, 6, 7, 8, 9, 10 };
	    for (int n = 0; n < range.length; n++)
	    {
	    	final String label = "Range x " + range[n];  //String.format("%f", multiple[n]);
	    	rbMenuItem = new JRadioButtonMenuItem(label);
	    	rbMenuItem.setSelected(Stippler.dotRange == range[n]);
	    	rbMenuItem.addActionListener(al);
	    	group.add(rbMenuItem);
	    	subMenu.add(rbMenuItem);
	    }
	    menu.add(subMenu);	    
	    
	    menu.addSeparator();

	    subMenu = new JMenu("Oversample");
	    subMenu.setMnemonic(KeyEvent.VK_O);	    
	    group = new ButtonGroup();
	    for (int n = 1; n <= 8; n++)
	    {
	    	final String label = n + " Per Pixel";
	    	rbMenuItem = new JRadioButtonMenuItem(label);
	    	rbMenuItem.setSelected(Stippler.overSample == n);
	    	rbMenuItem.addActionListener(al);
	    	group.add(rbMenuItem);
	    	subMenu.add(rbMenuItem);
	    }
	    menu.add(subMenu);	    
	    
	    subMenu = new JMenu("Blur Amount");
	    subMenu.setMnemonic(KeyEvent.VK_B);	    
	    group = new ButtonGroup();
	    for (int n = 1; n <= 8; n++)
	    {
	    	final String label = "Blur x " + n;
	    	rbMenuItem = new JRadioButtonMenuItem(label);
	    	rbMenuItem.setSelected(Stippler.blurFactor == n);
	    	rbMenuItem.addActionListener(al);
	    	group.add(rbMenuItem);
	    	subMenu.add(rbMenuItem);
	    }
	    menu.add(subMenu);	    
	    
	    //menu.addSeparator();
	    
	    subMenu = new JMenu("Cell Buffer");
	    subMenu.setMnemonic(KeyEvent.VK_B);	    
	    group = new ButtonGroup();
	    final int[] buffer = { 1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024 };
	    for (int n = 0; n < buffer.length; n++)
	    {
	    	final String label = buffer[n] + " x " + buffer[n];
	    	rbMenuItem = new JRadioButtonMenuItem(label);
	    	rbMenuItem.setSelected(Stippler.cellBuffer == buffer[n]);
	    	rbMenuItem.addActionListener(al);
	    	group.add(rbMenuItem);
	    	subMenu.add(rbMenuItem);
	    }
	    menu.add(subMenu);	    
	   
	    menu.addSeparator();
	    
	    subMenu = new JMenu("White Threshold");
	    subMenu.setMnemonic(KeyEvent.VK_B);	    
	    group = new ButtonGroup();
	    final int[] white = { 0, 10, 20, 30, 40, 50, 60, 70, 75, 80, 85, 90, 95, 98, 99, 100, 101 };
	    for (int n = 0; n < white.length; n++)
	    {
	    	final String label = white[n] + "%";
	    	rbMenuItem = new JRadioButtonMenuItem(label);
	    	rbMenuItem.setSelected(Stippler.whiteThreshold == white[n]);
	    	rbMenuItem.addActionListener(al);
	    	group.add(rbMenuItem);
	    	subMenu.add(rbMenuItem);
	    }
	    menu.add(subMenu);	    

//		cbMenuItem = new JCheckBoxMenuItem("Exclude White");
//		menuItem.setMnemonic(KeyEvent.VK_W);
//	    menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.META_MASK));
//	    cbMenuItem.setSelected(Stippler.excludeWhite);
//	    cbMenuItem.addItemListener(il);
//	    menu.add(cbMenuItem);    

	    //----------------------------------------
	    //	"Help" menu
	    menu = new JMenu("Help");
	    menu.setMnemonic(KeyEvent.VK_N);
	    menu.getAccessibleContext().setAccessibleDescription("Help items.");
	    this.add(menu);
	   
	    menuItem = new JMenuItem("About", KeyEvent.VK_A);
	    menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.META_MASK));
	    menuItem.addActionListener(al);
	    menu.add(menuItem);	
	}

}
