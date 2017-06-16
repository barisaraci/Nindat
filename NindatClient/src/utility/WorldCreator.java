package utility;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class WorldCreator {
	
	private World world;
	private TiledMap tiledMap;
	
	private List<Rectangle> rects = new ArrayList<>();
	
	private int map[][];
	
	public WorldCreator (World world, TiledMap tiledMap) {
		this.world = world;
		this.tiledMap = tiledMap;
	}
	
	public void create() {
		TiledMapTileLayer layer = (TiledMapTileLayer) tiledMap.getLayers().get("static");
		
		map = new int[layer.getHeight()][layer.getWidth()];
		
		for (int row = 0; row < layer.getHeight(); row++) {
			for (int col = 0; col < layer.getWidth(); col++) {
				Cell cell = layer.getCell(col, row);
				
				if (cell == null || cell.getTile() == null)
					continue;
				
				map[row][col] = cell.getTile().getId();
			}
		}
		
		findBiggestRectangle(map);
		
		BodyDef bdef = new BodyDef();
		FixtureDef fdef = new FixtureDef();
		PolygonShape ps = new PolygonShape();
		
		float tileSize = layer.getTileWidth();
		
		for (Rectangle rect : rects) {
			bdef.type = BodyType.StaticBody;
			bdef.position.set((rect.y * tileSize + (rect.w * tileSize / 2)) / Variables.PPM, (rect.x * tileSize + (rect.h * tileSize / 2)) / Variables.PPM);
			
			ps.setAsBox(rect.w * tileSize / 2 / Variables.PPM, rect.h * tileSize / 2 / Variables.PPM);
			fdef.density = 1f;
			fdef.friction = 0.2f;
			fdef.shape = ps;
			fdef.filter.categoryBits = Variables.BIT_STATIC;
			world.createBody(bdef).createFixture(fdef).setUserData("static_" + rect.tileId);
		}
		
		ps.dispose();
	}

    private void findBiggestRectangle(int[][] matrix) {
        Point best_ll = new Point(0, 0);
        Point best_ur = new Point(-1, -1);
        int best_area = 0;

        final int MaxX = matrix[0].length;
        final int MaxY = matrix.length;

        Stack<Point> stack = new Stack<Point>();
        int[] cache = new int[MaxX + 1];

        for (int m = 0; m != MaxX + 1; m++) {
            cache[m] = 0;
        }

        for (int n = 0; n != MaxY; n++) {
            int openWidth = 0;
            cache = updateCache(cache, matrix[n], MaxX);
            for (int m = 0; m != MaxX + 1; m++) {
                if (cache[m] > openWidth) {
                    stack.push(new Point(m, openWidth));
                    openWidth = cache[m];
                } else if (cache[m] < openWidth) {
                    int area;
                    Point p;
                    do {
                        p = stack.pop();
                        area = openWidth * (m - p.x);
                        if (area > best_area) {
                            best_area = area;
                            best_ll.x = p.x;
                            best_ll.y = n;
                            best_ur.x = m - 1;
                            best_ur.y = n - openWidth + 1;
                        }
                        openWidth = p.y;
                    } while (cache[m] < openWidth);
                    openWidth = cache[m];
                    if (openWidth != 0) {
                        stack.push(p);
                    }
                }
            }
        }
        
        int x, y, w, h;
        
        x = (best_ll.y > best_ur.y) ? best_ur.y : best_ll.y;
        y = (best_ll.x > best_ur.x) ? best_ur.x : best_ll.x;
        w = Math.abs(best_ur.x - best_ll.x + 1);
        h = Math.abs(best_ur.y - best_ll.y) + 1;
        
        if (x < 0 || y < 0)
        	return;
        
        Rectangle rect = new Rectangle(x, y, w, h);
        rect.tileId = map[rect.x][rect.y];
        rects.add(rect);
        
        for (int row = rect.x; row < rect.x + rect.h; row++) {
        	for (int col = rect.y; col < rect.y + rect.w; col++) {
        		map[row][col] = 0;
        	}
        }
        
        if (w == 0 || h == 0)
        	return;
        
        findBiggestRectangle(map);
        
    }
    
    private int[] updateCache(int[] cache, int[] matrixRow, int MaxX) {
        for (int m = 0; m < MaxX; m++) {
            if (matrixRow[m] == 0) {
                cache[m] = 0;
            } else {
                cache[m]++;
            }
        }
        return cache;
    }
    
    class Point {
    	public int x, y;
    	
    	public Point (int x, int y) {
    		this.x = x;
    		this.y = y;
    	}
    }
    
    class Rectangle {
    	public int x, y, w, h, tileId;
    	
    	public Rectangle (int x, int y, int w, int h) {
    		this.x = x;
    		this.y = y;
    		this.w = w;
    		this.h = h;
    	}
    }

}
