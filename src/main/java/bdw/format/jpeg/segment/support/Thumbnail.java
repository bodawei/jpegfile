/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bdw.format.jpeg.segment.support;

import bdw.format.jpeg.support.Problem;
import bdw.format.jpeg.support.InvalidJpegFormat;
import bdw.format.jpeg.support.ParseMode;
import bdw.io.LimitingDataInput;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author bodawei
 */
public class Thumbnail {
	private int width;
	private int height;
	protected List<Problem> problems;

	public Thumbnail() {
		width = 0;
		height = 0;
		problems = new ArrayList<Problem>();
	}
	
	public void setWidth(int width) {
		ParamCheck.checkByte(width);
		
		this.width = width;
		checkProblems();
	}

	public int getWidth() {
		return this.width;
	}

	public void setHeight(int height) {
		ParamCheck.checkByte(height);

		this.height = height;
		checkProblems();
	}

	public int getHeight() {
		return this.height;
	}

	public List<Problem> getProblems() {
		checkProblems();
		return problems;
	}

	protected void checkProblems() {
		problems.clear();
	}

	public void write(OutputStream stream) throws IOException {
		
	}

	public void readData(LimitingDataInput limited, ParseMode mode) throws IOException, InvalidJpegFormat {

	}
}
