package message.grid;

import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * A single potential pattern individual for the GA.
 * @author kg249
 */
public class Pattern implements Comparable<Pattern> {
	
	private static final double MUTATION = 0.05;
	
	private final boolean[] white; //white or black pixels
	private final int width;
	private final int height;
	
	private int error;
	private double fitness;
	
	public Pattern(int width, int height, Random rand) {
		white = new boolean[width*height];
		this.width = width;
		this.height = height;
		
		for(int i=0; i<white.length; i++) {
			white[i] = rand.nextBoolean();
		}
	}
	
	public Pattern(int width, int height) {
		white = new boolean[width*height];
		this.width = width;
		this.height = height;
	}
	
	public Pattern(Pattern a, Pattern b, Random rand) {
		white = new boolean[a.white.length];
		this.width = a.width;
		this.height = a.height;
		
		for(int i=0; i<white.length; i++) {
			if(rand.nextDouble()<MUTATION)
				white[i] = rand.nextBoolean();
			else
				white[i] = (rand.nextBoolean() ? a : b).white[i];
		}
	}
	
	public int getWidth() { return width; }
	public int getHeight() { return height; }
	
	public boolean isWhite(int x, int y) { return white[y*width+x]; }
	
	public void computeError(MessageFamily[] families) {
		error = 0;
		for(MessageFamily mf : families)
			error += mf.getMinimumError(this);
		fitness = 1.0 / (error+1.0);
	}

	@Override
	public int compareTo(Pattern o) {
		return Integer.compare(error, o.error);
	}

	public int getError() {
		return error;
	}

	public double getFitness() {
		return fitness;
	}
	
	public BufferedImage toImage() {
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		for(int x=0; x<width; x++)
			for(int y=0; y<height; y++)
				img.setRGB(x, y, isWhite(x,y)?0xFFFFFF:0x000000);
		return img;
	}
	
}
