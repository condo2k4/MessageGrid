package message.grid;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;

/**
 * A single valid message. Each Message is made up of multiple tri-colour images.
 * Each image has a location within the message. White pixels are as-is, blue
 * pixels indicate allowed transparency. All other colours are treated as black.
 * @author kg249
 */
public class Message {
	private final Collection<OffsetImage> images = new ArrayList<>();
	
	public Message() {}
	
	public Message addImage(int x, int y, BufferedImage img) {
		images.add(new OffsetImage(x, y, img));
		return this;
	}
	
	public boolean matches(int x, int y, boolean isWhite) {
		for(OffsetImage oi : images) {
			if(oi.isInBounds(x, y))
				return oi.isWhite(x, y)==isWhite;
		}
		return true;
	}
	
	public int getError(Pattern pattern) {
		int err = 0;
		for(int x=0; x<pattern.getWidth(); x++) {
			for(int y=0; y<pattern.getHeight(); y++) {
				boolean isWhite = pattern.isWhite(x, y);
				if(!matches(x, y, isWhite))
					err+=isWhite?1:2;
			}
		}
		return err;
	}
	
	public BufferedImage toImage(Pattern pattern) {
		BufferedImage img = new BufferedImage(pattern.getWidth(), pattern.getHeight(), BufferedImage.TYPE_INT_ARGB);
		for(int x=0; x<pattern.getWidth(); x++)
			for(int y=0; y<pattern.getHeight(); y++) {
				img.setRGB(x, y, 0xFF00FFFF);
				for(OffsetImage oi : images) {
					if(oi.isInBounds(x, y)) {
						if(oi.isWhite(x,y)!=pattern.isWhite(x, y))
							img.setRGB(x, y, oi.isWhite(x,y)?0xFFFFFFFF:0xFF000000);
						else
							img.setRGB(x, y, 0xFFFF0000);
						break;
					}
				}
			}
		return img;
	}
	
	private static class OffsetImage {
	
		private final int x, y;
		private final BufferedImage img;

		public OffsetImage(int x, int y, BufferedImage img) {
			this.x = x;
			this.y = y;
			this.img = img;
		}

		public int getX() { return x; }
		public int getY() { return y; }
		public int getWidth() { return img.getWidth(); }
		public int getHeight() { return img.getHeight(); }
		public BufferedImage getImg() { return img; }
		
		public boolean isInBounds(int x, int y) {
			if(x>=this.x && x<this.x+img.getWidth() & y>=this.y && y<this.y+img.getHeight()) {
				return img.getRGB(x-this.x, y-this.y)!=0xFF0000FF;
			}
			return false;
		}
		
		public boolean isWhite(int x, int y) {
			return img.getRGB(x-this.x, y-this.y)==0xFFFFFFFF;
		}
	}
}