package message.grid;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Main method containing UI and genetic algorithm.
 * 
 * @author kg249
 */
public class MessageGrid {

	private final static int GENERATIONS = 10;
	private final static int INDIVIDUALS = 100;
	private final static int SPECIES = 15;
	
	private static final int ELITIST = 5;
	
	private static Pattern roulette(Pattern[] patterns, double fitnessSum, Random random) {
		double selected = random.nextDouble()*fitnessSum;
		int i=0;
		while(selected>patterns[i].getFitness()) {
			selected-=patterns[i].getFitness();
			i++;
		}
		return patterns[i];
	}
	
	private static final int SCALE = 4;
	private static BufferedImage scaled(BufferedImage img) {
		BufferedImage dest = new BufferedImage(img.getWidth()*SCALE, img.getHeight()*SCALE, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = dest.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		g.drawImage(img, 0, 0, img.getWidth()*SCALE, img.getHeight()*SCALE, null);
		g.dispose();
		return dest;
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) throws IOException {
		//Load original images
		BufferedImage father = ImageIO.read(new File("father.png"));
		BufferedImage courage = ImageIO.read(new File("courage.png"));
		BufferedImage huella = ImageIO.read(new File("huella.png"));
		BufferedImage humana = ImageIO.read(new File("humana.png"));
		BufferedImage hymn_ = ImageIO.read(new File("hymn.png"));
		BufferedImage rose = ImageIO.read(new File("rose.png"));
		BufferedImage garden = ImageIO.read(new File("garden.png"));
		
		//Build each of the four families
		MessageFamily fatherCourage = new MessageFamily();
		for(int top=-1; top<=4; top++)
			fatherCourage.add(new Message().addImage(top, 0, father).addImage(0, 11, courage));
		
		MessageFamily huellaHumana = new MessageFamily();
		for(int top=-1; top<=4; top++) for(int bottom=-1; bottom<=4; bottom++)
			huellaHumana.add(new Message().addImage(top, 0, huella).addImage(bottom, 11, humana));
		
		MessageFamily hymn = new MessageFamily();
		for(int top=-1; top<=8; top++)
			for(int left=-1; left<=1; left++)
			hymn.add(new Message().addImage(left, top, hymn_));
		
		MessageFamily roseGarden = new MessageFamily();
		for(int top=-1; top<=24; top++) for(int bottom=-1; bottom<=4; bottom++)
			roseGarden.add(new Message().addImage(top, 0, rose).addImage(bottom, 11, garden));
		
		//Array of all families for simplicity
		MessageFamily[] messageFamilies = new MessageFamily[]{
			fatherCourage, huellaHumana, hymn, roseGarden
		};
		
		//Iniitalise GA
		Random random = new Random();
		Pattern[][] population = new Pattern[SPECIES][INDIVIDUALS];
		Comparator<Pattern[]> SPECIES_COMP = (a,b) -> a[0].compareTo(b[0]);
		
		System.out.println("Gen 0");
		for(int s=0; s<SPECIES; s++) {
			for(int i=0; i<INDIVIDUALS; i++) {
				population[s][i] = new Pattern(54, 22, random);
				population[s][i].computeError(messageFamilies);
			}
			Arrays.sort(population[s]);
			System.out.println(population[s][0].getError());
		}
		Arrays.sort(population, SPECIES_COMP);
		System.out.println();
		
		//Show UI with initial pattern
		JFrame frame = new JFrame("Output");
		JLabel baseLbl = new JLabel(new ImageIcon(scaled(population[0][0].toImage())));
		JLabel fatherCourageLbl = new JLabel(new ImageIcon(scaled(fatherCourage.getBestMessage(population[0][0]).toImage(population[0][0]))));
		JLabel huellaHumanaLbl = new JLabel(new ImageIcon(scaled(huellaHumana.getBestMessage(population[0][0]).toImage(population[0][0]))));
		JLabel hymnLbl = new JLabel(new ImageIcon(scaled(hymn.getBestMessage(population[0][0]).toImage(population[0][0]))));
		JLabel roseGardenLbl = new JLabel(new ImageIcon(scaled(roseGarden.getBestMessage(population[0][0]).toImage(population[0][0]))));
		
		fatherCourageLbl.setBackground(Color.RED);
		fatherCourageLbl.setOpaque(true);
		huellaHumanaLbl.setBackground(Color.RED);
		huellaHumanaLbl.setOpaque(true);
		hymnLbl.setBackground(Color.RED);
		hymnLbl.setOpaque(true);
		roseGardenLbl.setBackground(Color.RED);
		roseGardenLbl.setOpaque(true);
		
		frame.setLayout(new GridLayout(5, 2, 5, 5));
		frame.add(baseLbl);
		frame.add(new JLabel("Base"));
		frame.add(fatherCourageLbl);
		frame.add(new JLabel("Father Courage"));
		frame.add(huellaHumanaLbl);
		frame.add(new JLabel("Huella Humana"));
		frame.add(hymnLbl);
		frame.add(new JLabel("Hymn"));
		frame.add(roseGardenLbl);
		frame.add(new JLabel("Rose Garden"));
		
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		
		//Iterate through generations, showing current state on UI
		for(int g=1; g<GENERATIONS; g++) {
			System.out.println("Gen "+g);
			
			for(int s=0; s<SPECIES; s++) {
				
				double fitnessSum = 0.0;
				for(Pattern p : population[s])
					fitnessSum+=p.getFitness();
				
				Pattern[] nextGen = new Pattern[INDIVIDUALS];
				System.arraycopy(population[s], 0, nextGen, 0, ELITIST);
				for(int i=ELITIST; i<INDIVIDUALS; i++) {
					Pattern pA = roulette(population[s], fitnessSum, random);
					Pattern pB = roulette(population[s], fitnessSum, random);
					nextGen[i] = new Pattern(pA, pB, random);
					nextGen[i].computeError(messageFamilies);
				}
				Arrays.sort(nextGen);
				population[s] = nextGen;
				System.out.println(population[s][0].getError());
				
				//Update UI
				baseLbl.setIcon(new ImageIcon(scaled(population[0][0].toImage())));
				fatherCourageLbl.setIcon(new ImageIcon(scaled(fatherCourage.getBestMessage(population[0][0]).toImage(population[0][0]))));
				huellaHumanaLbl.setIcon(new ImageIcon(scaled(huellaHumana.getBestMessage(population[0][0]).toImage(population[0][0]))));
				hymnLbl.setIcon(new ImageIcon(scaled(hymn.getBestMessage(population[0][0]).toImage(population[0][0]))));
				roseGardenLbl.setIcon(new ImageIcon(scaled(roseGarden.getBestMessage(population[0][0]).toImage(population[0][0]))));
			}
			Arrays.sort(population, SPECIES_COMP);
			System.out.println();
		}
		
		System.out.println("Best");
		System.out.println(population[0][0].getError());
		
		//Output multicolour overview images. (Not directly manufacturable)
		File dest = new File("output");
		dest.mkdirs();
		ImageIO.write(scaled(population[0][0].toImage()), "PNG", new File(dest, "base.png"));
		ImageIO.write(scaled(fatherCourage.getBestMessage(population[0][0]).toImage(population[0][0])), "PNG", new File(dest, "father courage.png"));
		ImageIO.write(scaled(huellaHumana.getBestMessage(population[0][0]).toImage(population[0][0])), "PNG", new File(dest, "huella humana.png"));
		ImageIO.write(scaled(hymn.getBestMessage(population[0][0]).toImage(population[0][0])), "PNG", new File(dest, "hymn.png"));
		ImageIO.write(scaled(roseGarden.getBestMessage(population[0][0]).toImage(population[0][0])), "PNG", new File(dest, "rose garden.png"));
		
		//Create editor frame
		JFrame editorFrame = new JFrame("Editor");
		JPanel pane = new JPanel(new GridLayout(4, 1, 5, 5));
		EditorPane editorFatherCourage = new EditorPane(population[0][0], fatherCourage.getBestMessage(population[0][0]));
		EditorPane editorHuellaHumana = new EditorPane(population[0][0], huellaHumana.getBestMessage(population[0][0]));
		EditorPane editorHymn = new EditorPane(population[0][0], hymn.getBestMessage(population[0][0]));
		EditorPane editorRoseGarden = new EditorPane(population[0][0], roseGarden.getBestMessage(population[0][0]));
		pane.add(editorFatherCourage);
		pane.add(editorHuellaHumana);
		pane.add(editorHymn);
		pane.add(editorRoseGarden);
		editorFrame.add(pane);
		JButton btn = new JButton("Save");
		btn.addActionListener(e -> {
			if(!editorFatherCourage.isFullyConnected()) {
				JOptionPane.showMessageDialog(editorFrame, "Message 'Father Courage' is not fully connected.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			if(!editorHuellaHumana.isFullyConnected()) {
				JOptionPane.showMessageDialog(editorFrame, "Message 'Huella Humana' is not fully connected.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			if(!editorHymn.isFullyConnected()) {
				JOptionPane.showMessageDialog(editorFrame, "Message 'Hymn' is not fully connected.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			if(!editorRoseGarden.isFullyConnected()) {
				JOptionPane.showMessageDialog(editorFrame, "Message 'Rose Garden' is not fully connected.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			try {
				ImageIO.write(scaled(editorFatherCourage.getCutImage()), "PNG", new File("output\\fatherCourageCut.png"));
				ImageIO.write(scaled(editorFatherCourage.getEngraveImage()), "PNG", new File("output\\fatherCourageEngrave.png"));
				ImageIO.write(scaled(editorHuellaHumana.getCutImage()), "PNG", new File("output\\huellaHumanaCut.png"));
				ImageIO.write(scaled(editorHuellaHumana.getEngraveImage()), "PNG", new File("output\\huellaHumanaEngrave.png"));
				ImageIO.write(scaled(editorHymn.getCutImage()), "PNG", new File("output\\hymnCut.png"));
				ImageIO.write(scaled(editorHymn.getEngraveImage()), "PNG", new File("output\\hymnEngrave.png"));
				ImageIO.write(scaled(editorRoseGarden.getCutImage()), "PNG", new File("output\\roseGardenCut.png"));
				ImageIO.write(scaled(editorRoseGarden.getEngraveImage()), "PNG", new File("output\\roseGardenEngrave.png"));
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		});
		editorFrame.add(btn, BorderLayout.SOUTH);
		editorFrame.pack();
		editorFrame.setLocationRelativeTo(null);
		editorFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		editorFrame.setVisible(true);
	}
	
}
