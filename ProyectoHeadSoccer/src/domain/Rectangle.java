package domain;

public class Rectangle extends Figure{
      private int width;
      private int height;
	  
      
      public int getWidth() {
		  return width;
	  }
	  public void setWidth(int width) {
		  this.width = width;
	  }
	  public int getHeight() {
		  return height;
	  }
	  public void setHeight(int height) {
		  this.height = height;
	  }
	  
	  @Override
	  public boolean detectCollisionWithCircle(Circle circle) {
		  int xCircle = circle.x;
		  int yCircle = circle.y;
		  int radiousCircle = circle.getRadious();
		  
		  return xCircle + radiousCircle >= this.x
				  && xCircle - radiousCircle <= this.x + this.width
				  && yCircle + radiousCircle >= this.y
				  && yCircle - radiousCircle <= this.y + this.height;
	  }
	  
	  @Override
	  public boolean detectCollisionWithRectangle(Rectangle rectangle) {
		  int xRec = rectangle.x;
		  int yRec = rectangle.y;
		  int wRec = rectangle.width;
		  int hRec = rectangle.height;
		  
		  return xRec + wRec >= this.x
				  && xRec <= this.x + this.width
				  && yRec + hRec >= this.y
				  && yRec <= this.y + this.height;
	  }
}
