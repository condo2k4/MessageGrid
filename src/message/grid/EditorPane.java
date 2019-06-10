package message.grid;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Random;
import javax.swing.JComponent;

/**
 * Allows for clean-up and refining of the pattern and messages produced by
 * the GA. Provides an image that can be used to engrave and another that can
 * be used to cut the message decoders.
 * 
 * @author kg249
 */
public class EditorPane extends JComponent {
	
	private static final int SCALE = 10;
	private static final int GAP = 10;
	
	private static final Color TRANSPARENT_BLACK = new Color(245,0,245);
	private static final Color TRANSPARENT_WHITE = Color.MAGENTA;
	private static final Color OPAQUE_BLACK = Color.BLACK;
	private static final Color OPAQUE_WHITE = Color.WHITE;
	
	private static final Color TRANSPARENT = Color.WHITE;
	private static final Color CONNECTED = Color.BLACK;
	private static final Color UNCONNECTED = Color.BLUE;

	private final Pattern pattern;
	private final Message message;
	private final boolean[] transparent;
	private final boolean[] edgeConnected;
	private final int rightOffset;
	
	public EditorPane(Pattern pattern, Message message) {
		this.pattern = pattern;
		this.message = message;
		setPreferredSize(new Dimension(pattern.getWidth()*SCALE*2+GAP, pattern.getHeight()*SCALE));
		transparent = new boolean[pattern.getWidth()*pattern.getHeight()];
		edgeConnected = new boolean[pattern.getWidth()*pattern.getHeight()];
		
		for(int x=0; x<pattern.getWidth(); x++)
			for(int y=0; y<pattern.getHeight(); y++)
				transparent[y*pattern.getWidth() + x]=message.matches(x, y, pattern.isWhite(x, y));
		
		rightOffset = pattern.getWidth()*SCALE+GAP;
		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				int x;
				if(e.getX() > rightOffset)
					x = (e.getX()-rightOffset)/SCALE;
				else
					x = e.getX()/SCALE;
				int y = e.getY()/SCALE;
				if(x<0 || x>=pattern.getWidth()) return;
				if(y<0 || y>=pattern.getHeight()) return;
				if(!message.matches(x, y, pattern.isWhite(x, y))) return;
				transparent[y*pattern.getWidth() + x] = !transparent[y*pattern.getWidth() + x];
				recomputeEdgeConnected();
				repaint();
			}
		});
		recomputeEdgeConnected();
	}
	
	private void recomputeEdgeConnected() {
		Arrays.fill(edgeConnected, false);
		for(int x=0; x<pattern.getWidth(); x++) {
			edgeConnected[x] = !transparent[x];
			edgeConnected[x+(pattern.getHeight()-1)*pattern.getWidth()] = !transparent[x+(pattern.getHeight()-1)*pattern.getWidth()];
		}
		for(int y=0; y<pattern.getHeight(); y++) {
			edgeConnected[y*pattern.getWidth()] = !transparent[y*pattern.getWidth()];
			edgeConnected[pattern.getWidth()-1+y*pattern.getWidth()] = !transparent[pattern.getWidth()-1+y*pattern.getWidth()];
		}
		boolean changed;
		do {
			changed = false;
			for(int x=1; x<pattern.getWidth(); x++)
				for(int y=1; y<pattern.getHeight(); y++)
					if(!transparent[y*pattern.getWidth() + x] && !edgeConnected[y*pattern.getWidth() + x]) {
						if(
								(edgeConnected[y*pattern.getWidth() + x-1]) ||
								(edgeConnected[(y-1)*pattern.getWidth() + x]) ||
								(edgeConnected[y*pattern.getWidth() + x+1]) ||
								(edgeConnected[(y+1)*pattern.getWidth() + x])) {
							edgeConnected[y*pattern.getWidth() + x] = true;
							changed = true;
						}
					}
		} while(changed);
	}
	
	public boolean whiteMessagePixel(int x, int y) {
		if(message.matches(x, y, true)) {
			if(message.matches(x, y, false)) { //matches() always returns true for transparent pixels
				long seed = (x<<8)|y|((x*31L)<<16)^((y*27)<<4);
				Random r = new Random(seed);
				for(int i=0; i<20; i++) r.nextBoolean();
				return r.nextBoolean();
			}
			return true;
		}
		return false;
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D)g.create();
		
		//Left side of editor - what the message + pattern would look like
		for(int x=0; x<pattern.getWidth(); x++)
			for(int y=0; y<pattern.getHeight(); y++) {
				if(transparent[y*pattern.getWidth() + x])
					g2.setColor(pattern.isWhite(x, y)?TRANSPARENT_WHITE:TRANSPARENT_BLACK);
				else
					g2.setColor(whiteMessagePixel(x, y)?OPAQUE_WHITE:OPAQUE_BLACK);
				g2.fillRect(x*SCALE, y*SCALE, SCALE, SCALE);
		}
		
		//Right side of editor - the final shape of the message, highlighting unconnected pieces
		for(int x=0; x<pattern.getWidth(); x++)
			for(int y=0; y<pattern.getHeight(); y++) {
				if(transparent[y*pattern.getWidth() + x])
					g2.setColor(TRANSPARENT);
				else
					g2.setColor(edgeConnected[x+y*pattern.getWidth()]?CONNECTED:UNCONNECTED);
				g2.fillRect(rightOffset + x*SCALE, y*SCALE, SCALE, SCALE);
		}
	}
	
	public BufferedImage getEngraveImage() {
		BufferedImage img = new BufferedImage(pattern.getWidth(), pattern.getHeight(), BufferedImage.TYPE_INT_ARGB);
		for(int x=0; x<pattern.getWidth(); x++)
			for(int y=0; y<pattern.getHeight(); y++) {
				img.setRGB(x, y, (!transparent[y*pattern.getWidth() + x]) && message.matches(x, y, false) ? 0xFF000000 : 0xFFFFFFFF);
			}
		return img;
	}
	
	public BufferedImage getCutImage() {
		BufferedImage img = new BufferedImage(pattern.getWidth(), pattern.getHeight(), BufferedImage.TYPE_INT_ARGB);
		for(int x=0; x<pattern.getWidth(); x++)
			for(int y=0; y<pattern.getHeight(); y++) {
				if(transparent[y*pattern.getWidth() + x])
					img.setRGB(x, y, (transparent[y*pattern.getWidth() + x]) ? 0xFF000000 : 0xFFFFFFFF);
			}
		return img;
	}
	
	public boolean isFullyConnected() {
		for(int i=0; i<transparent.length; i++)
			if(!transparent[i] && !edgeConnected[i])
				return false;
		return true;
	}
	
	
}
