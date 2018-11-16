package stippling.main;

import java.awt.event.*;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import stippling.graphics.StippleView;
import stippling.graphics.DotView;
import stippling.graphics.GlobeView;

//-----------------------------------------------------------------------------

/**
 * The app's main frame object.
 */
public class Stipple implements ActionListener, ItemListener 
{	
	/** Application name. */
	public static String appName = "Stipple 1.0";	

	/** The main frame instance. */
	static JFrame mainFrame;

	/** */
	static JFrame mapFrame;

	/** */
	static JFrame dotFrame;
	
	/** */
	static JFrame globeFrame;
	
	/** The single puzzle view instance. */
	private static StippleView mainView;

	/** The single puzzle view instance. */
	private static DotView dotView;

	/** The single puzzle view instance. */
	private static GlobeView globeView;
	
	/** */
	protected Stippler stippler;
	
	//-------------------------------------------------------------------------

	/**
	 * @return Main view.
	 */
	public StippleView mainView()
	{
		return mainView;
	}

	/**
	 * @return Dot view.
	 */
	public DotView dotView()
	{
		return dotView;
	}
	
	/**
	 * @return Globe view.
	 */
	public GlobeView globeView()
	{
		return globeView;
	}
	
	/**
	 * @return Stippler.
	 */
	public Stippler stippler()
	{
		return stippler;
	}
	
	/**
	 * Perform one iteration of relaxation.
	 */
	public void iterate()
	{
		stippler.relax();
		//stippler.relaxMultithreaded(4);
	}

	//-------------------------------------------------------------------------
	//	Menu handling
	
	/**
	 * Responds to an ItemEvent action.
	 * @args e the ItemEvent.
	 */
	public void actionPerformed(ActionEvent e) 
	{
		JMenuItem source = (JMenuItem)(e.getSource());		
		System.out.println("Menu action: " + source.getText() + ".");
		
		if (source.getText().equals("About"))
		{
			showAboutDialog();
		}
		else if (source.getText().equals("Load Image"))
		{
			final String path = selectFile(mainFrame, true, "All Image Files", "jpg", "png", "gif", "tif", "tiff", "bmp");
			if (path != null)
			{
				System.out.println(path + " selected.");
				stippler.loadImage(path);
			}
		}
		else if (source.getText().equals("Load Dots"))
		{
			final String path = selectFile(mainFrame, true, "Text File", "txt");
			if (path != null)
			{
				System.out.println(path + " selected.");
				stippler.loadDots(path);
			}
		}
		else if (source.getText().equals("Save Dots"))
		{
			stippler.setDotRadii();
			stippler.exportSVG(true);
			stippler.exportSVG(false);
			stippler.exportRaw();
			System.out.println("Done.");
		}
		else if (source.getText().equals("Save Mesh"))
		{
			stippler.exportMesh();
			System.out.println("Done.");
		}
		else if (source.getText().equals("Start"))
		{
			
			Thread thread = new Thread() {
		        public void run() 
		        {
		        	stippler.start();
		        }
		    };
		    thread.start();
		}

		else if (source.getText().equals("Stop"))
		{
			stippler.stop();
		}
		else if (source.getText().equals("Quit"))
		{
			System.exit(0);
		}
		else if (source.getText().contains(" Dots"))
		{
			final String sub = source.getText().substring(0, source.getText().indexOf(" Dots"));
			Stippler.numDots = Integer.parseInt(sub);  //.intValue();
			stippler.stop();
			stippler.createInitialDots();
		}
		else if (source.getText().contains("Blur x "))
		{
			final String sub = source.getText().substring(7);
			Stippler.blurFactor = Integer.parseInt(sub);
			stippler.reloadImage();
		}
		else if (source.getText().contains("Scale x "))
		{
			final String sub = source.getText().substring(8);
			Stippler.dotSize = Double.parseDouble(sub);
			stippler.setDotRadii();
			//dotView.invalidate();
			dotView.paintImmediately(0, 0, dotView.getWidth(), dotView.getHeight()); 
			globeView.paintImmediately(0, 0, globeView.getWidth(), globeView.getHeight()); 
		}
		else if (source.getText().contains("Range x"))
		{
			final String sub = source.getText().substring(8);
			Stippler.dotRange = Double.parseDouble(sub);
			stippler.setDotRadii();
			//dotView.invalidate();
			dotView.paintImmediately(0, 0, dotView.getWidth(), dotView.getHeight()); 
			globeView.paintImmediately(0, 0, globeView.getWidth(), globeView.getHeight()); 
		}
		else if (source.getText().contains(" x "))
		{
			final String sub = source.getText().substring(0, source.getText().indexOf(" x "));
			Stippler.cellBuffer = Integer.parseInt(sub);
		}
		else if (source.getText().contains(" Passes"))
		{
			final String sub = source.getText().substring(0, source.getText().indexOf(" Passes"));
			Stippler.numPasses = Integer.parseInt(sub);
		}
		else if (source.getText().contains(" Per Pixel"))
		{
			final String sub = source.getText().substring(0, source.getText().indexOf(" Per Pixel"));
			Stippler.overSample = Integer.parseInt(sub);
			stippler.reloadImage();
		}
		else if (source.getText().contains("%"))
		{
			final String sub = source.getText().substring(0, source.getText().indexOf("%"));
			Stippler.whiteThreshold = Integer.parseInt(sub);
			dotView.paintImmediately(0, 0, dotView.getWidth(), dotView.getHeight()); 
			globeView.paintImmediately(0, 0, globeView.getWidth(), globeView.getHeight()); 
		}
	}
	
	/**
	 * Responds to an ItemEvent change.
	 * @args e the ItemEvent.
	 */
	public void itemStateChanged(ItemEvent e) 
	{
		JMenuItem source = (JMenuItem) (e.getSource());
		System.out.println("Menu state: " + source.getText() + ", check: " + ((e.getStateChange() == ItemEvent.SELECTED) ? "selected" : "unselected"));	
	
		if (source.getText().equals("Pause"))
		{
			stippler.pause();
		}

	}
	
	//--------------------------------------------------------------------------
	
	/**
	 * Show the About dialog.
	 */
	public void showAboutDialog()
	{
		JOptionPane.showMessageDialog
		(	
			mainFrame,
			"\nMoves the dots to form an image.\n\n" +
			"Game and app by Cameron Browne (c) 2016.\n\n", 
			appName,
		    JOptionPane.PLAIN_MESSAGE
		);	
	}

	//--------------------------------------------------------------

	/**
	 * Opens a file selector dialog box for opening/saving a file
	 * @param parent 
	 * @param isOpen True if the file will be opened, false if it will be saved. 
	 * @param description 
	 * @param extensions List of acceptable file types.
	 * @return User-selected file, or null if user cancelled.
	 */
	public static final String selectFile
	(
		final JFrame parent, final boolean isOpen, final String description, final String... extensions
	)
	{
		String baseFolder = System.getProperty("user.dir");		
		String subFolder = baseFolder + "/"+extensions[0];
		File testFile = new File(subFolder);
		if (!testFile.exists())
			subFolder = baseFolder;  // no suitable subfolder - let user find them
		
		JFileChooser dlg = new JFileChooser(subFolder);
				
		//	Set the file filter to show suitable files only
		FileFilter filter = new FileNameExtensionFilter(description, extensions);
		dlg.setFileFilter(filter);
		
		int response;
		if (isOpen)
			response = dlg.showOpenDialog(parent);
		else
			response = dlg.showSaveDialog(parent);
		
		if (response == JFileChooser.APPROVE_OPTION)
			return dlg.getSelectedFile().getAbsolutePath();
		
		return null;
	}

	//--------------------------------------------------------------

	/**
	 * Create the main frame and GUI components.
	 */
	void gui()
	{
		JFrame.setDefaultLookAndFeelDecorated(true);
		
		final int dim = 720;
		
		// Create the main frame
		mainFrame = new JFrame(appName);
		mainView  = new StippleView(this);
		mainFrame.setContentPane(mainView);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);		
	    mainFrame.setJMenuBar(new MainMenu(this, this));  // create view before menu bar?
		mainFrame.setSize(dim, dim);
		mainFrame.setLocation(100, 300);
        //mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);

		final int slx = mainFrame.getLocationOnScreen().x;
		final int sly = mainFrame.getLocationOnScreen().y;

		// Create the dot frame
		dotFrame = new JFrame("Dots View");
		dotView  = new DotView(this);
		dotFrame.setContentPane(dotView);
		dotFrame.setSize(dim, dim);
        dotFrame.setLocation(slx+(dim+1), sly);
        dotFrame.setVisible(true);

		// Create the globe frame
		globeFrame = new JFrame("Globe View");
		globeView  = new GlobeView(this);
		globeFrame.setContentPane(globeView);
		globeFrame.setSize(dim, dim);
	    //globeFrame.setLocation(slx+(dim+1)*2, sly);
	    globeFrame.setLocation(slx+(dim+1)*2-dim*3/2, sly+dim/2);
	    globeFrame.setVisible(true);

		stippler = new Stippler(this);		
    }

	//--------------------------------------------------------------
	
	/**
	 * Main routine.
	 * @param args Program arguments. 
	 */
	public static void main(String[] args) 
	{	
		Stipple app = new Stipple();
		app.gui();
	}

	//--------------------------------------------------------------

}
