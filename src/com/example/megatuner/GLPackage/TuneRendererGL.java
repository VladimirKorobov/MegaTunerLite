package com.example.megatuner.GLPackage;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.example.megatuner.Interfaces.TuneBitmap;
import com.example.megatuner.Interfaces.TuneRenderer;
import com.example.megatuner.TuneSurface;
import com.example.megatuner.TuneSurface.TextBuffer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.opengl.GLSurfaceView.Renderer;

public class TuneRendererGL implements TuneRenderer, Renderer{
	
	private class TextItem
	{
		public TextItem(String text, float x, float y)
		{
			textures = new float[text.length() * 4 * 2];
			vertices = new float[textures.length];
			getTextArrays(text, vertices, textures);
			this.x = x;
			this.y = y;
		}
		float[] vertices;
		float[] textures;
		float x;
		float y;
	}
	
	private class TextBufferGL  implements TuneSurface.TextBuffer
	{
		List<TextItem> textList = new ArrayList<TextItem>();

		@Override
		public void addText(String text, float x, float y) {
			
			textList.add(new TextItem(text, x, y));
		}

		@Override
		public void release() {
			textList.clear();
		}
	}
	
	GL10 gl;
	TuneSurface surface;

	// Font texture
	private static float nativeTextSize = 50;
	static int textureWidth = 1024;
	static float[] textureCoord;
	static int fontTexture = -1;
	static int textureFontDx;
	static int textureFontDy;
	static int textureCharInRow;
	
	//private FloatBuffer vertexBuffer = null;
	//private FloatBuffer textureBuffer = null;

    private IntBuffer vertexBufferInt = null;
    private IntBuffer textureBufferInt = null;
    private int[] intArray = new int[8];
    boolean test = true;


    TuneRendererGL(TuneSurface surface)
	{
		this.surface = surface;
		// Allocate buffers
        /*
		vertexBuffer = ReallocateBuffer(vertexBuffer, 2048 * 4 * 4); // (2048 * 4 vertices max
		textureBuffer = ReallocateBuffer(textureBuffer, 2048 * 4 * 4); // (2048 * 4 texture coord max
		*/

        vertexBufferInt = ReallocateBufferInt(vertexBufferInt, 2048 * 4); // (2048 * 4 vertices max
        textureBufferInt = ReallocateBufferInt(textureBufferInt, 2048 * 4); // (2048 * 4 texture coord max
	}

    /*
	private FloatBuffer ReallocateBuffer(FloatBuffer buffer, int newSize)
	{
		if(buffer == null || buffer.capacity() < newSize)
		{
			ByteBuffer byteBuffer = ByteBuffer.allocateDirect(newSize * 4);
			byteBuffer.order(ByteOrder.nativeOrder());
			        // allocates the memory from the byte buffer
			buffer = byteBuffer.asFloatBuffer();
		}
		buffer.position(0);
		return buffer;
	}
	
	
	
	private FloatBuffer FillBuffer(FloatBuffer buffer, float[] vertices)
	{
		buffer = ReallocateBuffer(buffer, vertices.length);
		buffer.put(vertices);
		buffer.position(0);
		return buffer;
	}
	*/

    private IntBuffer  ReallocateBufferInt(IntBuffer buffer, int newSize)
    {
        if(buffer == null || buffer.capacity() < newSize)
        {
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(newSize * 4);
            byteBuffer.order(ByteOrder.nativeOrder());
            // allocates the memory from the byte buffer
            buffer = byteBuffer.asIntBuffer();
        }
        buffer.position(0);
        return buffer;
    }



    private IntBuffer FillBufferInt(IntBuffer buffer, float[] vertices)
    {
        if( intArray.length < vertices.length)
        {
            intArray = new int[vertices.length];
        }
        for( int i = 0; i < vertices.length; i++ )
        {
            intArray[ i ] = Float.floatToIntBits(vertices[i]);
        }
        return FillBufferInt(buffer, intArray);
    }
    private IntBuffer FillBufferInt(IntBuffer buffer, int[] vertices)
    {
        buffer = ReallocateBufferInt(buffer, vertices.length);
        int capacity = buffer.capacity();

        buffer.put(intArray, 0, vertices.length);
        buffer.position(0);
        return buffer;
    }
	
	@Override
	public void setColor(int r, int g, int b, int a) {
		gl.glColor4f((float)r / 255, (float)g / 255, (float)b / 255, (float)a / 255);
	}

	@Override
	public void drawLine(float x0, float y0, float x1, float y1) {
        intArray[0] = Float.floatToIntBits(x0);
        intArray[1] = Float.floatToIntBits(y0);
        intArray[2] = Float.floatToIntBits(x1);
        intArray[3] = Float.floatToIntBits(y1);

		//vertexBuffer = FillBuffer(vertexBuffer, line);
        vertexBufferInt = FillBufferInt(vertexBufferInt, intArray);
        //gl.glVertexPointer(2, GL10.GL_FLOAT, 0, vertexBuffer);

        gl.glVertexPointer(2, GL10.GL_FLOAT, 0, vertexBufferInt);
		gl.glDrawArrays(GL10.GL_LINES, 0, 2);
	}

	@Override
	public void drawLines(float x, float y, float[] points) {
        gl.glPushMatrix();
		gl.glTranslatef(x, y, 0);
		
		//vertexBuffer = FillBuffer(vertexBuffer, points);
        vertexBufferInt = FillBufferInt(vertexBufferInt, points);
		//gl.glVertexPointer(2, GL10.GL_FLOAT, 0, vertexBuffer);
        gl.glVertexPointer(2, GL10.GL_FLOAT, 0, vertexBufferInt);
		gl.glDrawArrays(GL10.GL_LINES, 0, points.length / 2);
		gl.glPopMatrix();
	}

	@Override
	public void drawRect(float x0, float y0, float x1, float y1) {
		float[] coord = new float[] {
				x0, y1,
				x0, y0, 
				x1, y1, 
				x1, y0
		};
		
		vertexBufferInt = FillBufferInt(vertexBufferInt, coord);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, vertexBufferInt);
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, coord.length / 2);
	}

	private void getTextArrays(String text, float[] vertices, float[] textures)
	{
		int texIndex = 0;
		int vertIndex = 0;
		int i, j;
		
		int x0 = 0;
		for(i = 0; i < text.length(); i ++)
		{
			char ch = text.charAt(i);
			if(ch < 32 || ch > 128)
				ch = '?';
			int fontTextureIndex = (ch - 32) * 8;
			for(j = 0; j < 8; j ++)
			{
				textures[texIndex ++] = textureCoord[fontTextureIndex ++];
			}
			
			float width = (textures[texIndex - 4] - textures[texIndex - 8]) * textureWidth;
			
			// Left-bottom
			vertices[vertIndex ++] = x0;
			vertices[vertIndex ++] = textureFontDy;
			// Left-top
			vertices[vertIndex ++] = x0;
			vertices[vertIndex ++] = 0;
			// Right-bottom
			vertices[vertIndex ++] = x0 + width;
			vertices[vertIndex ++] = textureFontDy;
			// Right-top
			vertices[vertIndex ++] = x0 + width;
			vertices[vertIndex ++] = 0;
			
			x0 += width;
		}
	}
	
	private void drawTextTextures(float[] vertices, float[] textures, float x, float y, float textSize)
	{

        /*
		vertexBuffer = FillBuffer(vertexBuffer, vertices);
		textureBuffer = FillBuffer(textureBuffer, textures);
		*/
        vertexBufferInt = FillBufferInt(vertexBufferInt, vertices);
        textureBufferInt = FillBufferInt(textureBufferInt, textures);

		gl.glEnable(GL10.GL_TEXTURE_2D);
		
		gl.glBindTexture(GL10.GL_TEXTURE_2D, fontTexture);
		
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		
				
		gl.glEnable(GL10.GL_BLEND);
		gl.glEnable(GL10.GL_ALPHA_TEST);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		
				
/*
		 gl.glVertexPointer(2, GL10.GL_FLOAT, 0, vertexBuffer);
		 gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
		 */
        gl.glVertexPointer(2, GL10.GL_FLOAT, 0, vertexBufferInt);
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBufferInt);

		 
		 gl.glPushMatrix();
		 
		 float scale = textSize / nativeTextSize;
		 
		 gl.glTranslatef(x, y - textSize, 0);
		 gl.glScalef(scale, scale, 1);
		 
		 gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, vertices.length / 2);
		 
		 gl.glPopMatrix();
		
		 gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		 gl.glDisable(GL10.GL_TEXTURE_2D);
		 gl.glDisable(GL10.GL_BLEND);

	}
	
	@Override
	public void drawText(String text, float size, float x, float y) {
		if(fontTexture >= 0)
		{
			// Get texture coord of every symbol
			float[] textures = new float[text.length() * 4 * 2];
			float[] vertices = new float[textures.length];
			
			getTextArrays(text, vertices, textures);
			
			drawTextTextures(vertices, textures, x, y, size);
		}
	}

    @Override
    public void drawBitmap(float x, float y, TuneBitmap bitmap) {
        TuneBitmapGL bitmapGL = (TuneBitmapGL)bitmap;
        gl.glEnable(GL10.GL_TEXTURE_2D);

        float[] vertices = new float[4 * 2];
        float[] drawLinevertices = new float[4 * 2];
        float[] textures = new float[4 * 2];

        vertices[0] = x;
        vertices[1] = y + bitmap.getHeight();

        vertices[2] = x;
        vertices[3] = y;

        vertices[4] = x + bitmap.getWidth();
        vertices[5] = y + bitmap.getHeight();

        vertices[6] = x + bitmap.getWidth();
        vertices[7] = y;

        float factorX = bitmap.getWidth() / ((TuneBitmapGL) bitmap).textureWidth;
        float factorY = bitmap.getHeight() / ((TuneBitmapGL) bitmap).textureHeight;

        textures[0] = 0;
        textures[1] = factorY;

        textures[2] = 0;
        textures[3] = 0;

        textures[4] = factorX;
        textures[5] = factorY;

        textures[6] = factorX;
        textures[7] = 0;

        gl.glEnable(GL10.GL_BLEND);
        gl.glEnable(GL10.GL_ALPHA_TEST);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
        setColor(255, 255, 255, 255);

        //...and bind it to our array
        gl.glBindTexture(GL10.GL_TEXTURE_2D, bitmapGL.textureId[0]);

        //Create  Filtered Texture
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);

        //Use the Android GLUtils to specify a two-dimensional texture image from our bitmap
        vertexBufferInt = FillBufferInt(vertexBufferInt, vertices);
        textureBufferInt = FillBufferInt(textureBufferInt, textures);

        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

        gl.glVertexPointer(2, GL10.GL_FLOAT, 0, vertexBufferInt);
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBufferInt);

        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, vertices.length / 2);

        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glDisable(GL10.GL_TEXTURE_2D);
        gl.glDisable(GL10.GL_BLEND);
    }

    @Override
	public void onDrawFrame(GL10 gl) {
		this.gl = gl;
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		surface.draw();
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		// Set window dimensions
		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL10.GL_PROJECTION);  
		gl.glLoadIdentity();
		GLU.gluOrtho2D(gl, 0, width, height, 0);
		gl.glMatrixMode(GL10.GL_MODELVIEW);  
		gl.glLoadIdentity();
		this.gl = gl;
	
		// Update view layouts
		surface.updateLayouts(width, height);
	}

	@Override
	public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
		// Re-generate text texture
		Bitmap bmp = generateFontTexture();
		fontTexture = textureFromBmp(arg0, bmp);
		bmp.recycle();
	}
	/*
	static FloatBuffer DirectWrap(float[] buffer)
	{
		// a float has 4 bytes so we allocate for each coordinate 4 bytes
		ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect(buffer.length * 4);
		vertexByteBuffer.order(ByteOrder.nativeOrder());
		        // allocates the memory from the byte buffer
		FloatBuffer vertexBuffer = vertexByteBuffer.asFloatBuffer();
        
		
		vertexBuffer.put(buffer);
		vertexBuffer.position(0);
		return vertexBuffer;
	}
	*/
	static Bitmap generateFontTexture()
	{
		int i; 
		Paint paint = new Paint();
		paint.setTextSize(nativeTextSize);
		float[] widths = new float[1];
		paint.getTextWidths("W", widths);
		paint.getTextWidths("W", widths);
		textureFontDx = (int)(widths[0] + 0.5f);
		textureFontDy = (int)(nativeTextSize * 1.2f + 0.5f);
		int fontRange = 128 - 32;
		
		int bitmapWidth =  textureWidth;
		textureCharInRow = (int)(bitmapWidth / textureFontDx);
		int columnSize = (int)((fontRange + textureCharInRow - 1) / textureCharInRow);
		int h = columnSize * textureFontDy;
		int bitmapHeight = 1;
		while(bitmapHeight < h)
			bitmapHeight <<= 1;
		
		// Create an empty, mutable bitmap
		Bitmap bitmap = Bitmap.createBitmap((int)bitmapWidth, (int)bitmapHeight, Bitmap.Config.ALPHA_8);
		int w1 = bitmap.getWidth();
		int h1 = bitmap.getHeight();
		
		// get a canvas to paint over the bitmap
		Canvas canvas = new Canvas(bitmap);
		paint.setAlpha(0);
		canvas.drawPaint(paint);
		
		textureCoord = new float[fontRange * 4 * 2];
		// Draw the text
		paint.setAlpha(255);
		
		StringBuilder text = new StringBuilder(" ");
		i = 0;
		int texIndex = 0;
		float texXFactor = 1.f /  bitmapWidth;
		float texYFactor = 1.f /  bitmapHeight;
		
		for(int y = 0; y < columnSize && i < fontRange; y ++)
		{
			for(int x = 0; x < textureCharInRow && i < fontRange; x ++, i ++)
			{
				text.setCharAt(0, (char)(i + 32));
				
				String s = text.toString();
				canvas.drawText(s, x * textureFontDx, y * textureFontDy + nativeTextSize, paint);
				paint.getTextWidths(s, widths);
				
				float texLeft = x * textureFontDx * texXFactor;
				float texTop = y * textureFontDy * texYFactor;
				float texRight = texLeft + widths[0] * texXFactor;
				float texBottom = texTop + textureFontDy * texYFactor; 

				// Left-bottom
				textureCoord[texIndex ++] = texLeft;
				textureCoord[texIndex ++] = texBottom;
				// Left-top
				textureCoord[texIndex ++] = texLeft;
				textureCoord[texIndex ++] = texTop;
				// Right-bottom
				textureCoord[texIndex ++] = texRight;
				textureCoord[texIndex ++] = texBottom;
				// Right-top
				textureCoord[texIndex ++] = texRight;
				textureCoord[texIndex ++] = texTop;
			}
		}
		
		return bitmap;
	}
	
	static int textureFromBmp(GL10 gl, Bitmap bmp)
	{
		gl.glEnable(GL10.GL_TEXTURE_2D);
		
		//Generate one texture pointer...
		int[] textures = new int[1];
		gl.glGenTextures(1, textures, 0);
		//...and bind it to our array
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
		
		//Create  Filtered Texture
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

		//Different possible texture parameters, e.g. GL10.GL_CLAMP_TO_EDGE
		//gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT);
		//gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);

		//Use the Android GLUtils to specify a two-dimensional texture image from our bitmap
		
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bmp, 0);
		gl.glDisable(GL10.GL_TEXTURE_2D);
		
		return textures[0];
	}

	@Override
	public TextBuffer createTextBuffer() {
		return new TextBufferGL();
	}

	@Override
	public void setLineWidth(float lineWidth) {
		if(gl != null)
			gl.glLineWidth(lineWidth);
		
	}

	@Override
	public void drawTextBuffer(TextBuffer buffer, float size, float x, float y) {
		TextBufferGL bufferGL = (TextBufferGL)buffer;
		int length = bufferGL.textList.size();
		for(int i = 0; i < length; i ++ )
		{
			TextItem item = (TextItem)(bufferGL.textList.get(i));
			drawTextTextures(item.vertices, item.textures, x + item.x, y+ item.y , size);
		}
	}

    @Override
    public void save() {
        gl.glPushMatrix();
    }

    @Override
    public void restore() {
        gl.glPopMatrix();
    }

    @Override
    public void scale(float x, float y) {
        gl.glScalef(x, y, 1);
    }

    @Override
    public void scale(float x, float y, float cx, float cy) {
        gl.glTranslatef(cx, cy, 0);
        gl.glScalef(x, y, 1);
        gl.glTranslatef(-cx, -cy, 0);
    }

    @Override
    public void setClip(float x, float y, float width, float height) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void clearClip() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void rotate(float degrees, float xc, float yc) {
        gl.glTranslatef(xc, yc, 0);
        //gl.glRotatef((float)(degrees / 180 * Math.PI), 0, 0, 1 );
        gl.glRotatef(degrees, 0, 0, 1 );
        gl.glTranslatef(-xc, -yc, 0);
    }

    @Override
    public void setAntialias(Boolean antiAlias) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public float getTextWidth(String text, float textSize) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }
    @Override
    public TuneBitmap createBitmap(Bitmap bitmap){
        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);
        return new TuneBitmapGL(newBitmap, this.gl);  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public TuneBitmap createBitmap(Bitmap bitmap, int width, int height) {
        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        return new TuneBitmapGL(newBitmap, this.gl);
    }

}
