package mapper.util;

import org.lwjgl.glfw.GLFW;

public class MSCounter {
	private long deltaMs, numMs, lastMs;
	private double totalMs, avgMs;
	private final int targetFramerate;
	private final double msPerFrame;
	
	public MSCounter() {
		long monitor = GLFW.glfwGetPrimaryMonitor();
		targetFramerate = GLFW.glfwGetVideoMode(monitor).refreshRate();
		msPerFrame = (1000.0 / targetFramerate);

		lastMs = System.currentTimeMillis();
	}
	
	public void update() {
		long current = System.currentTimeMillis();
		deltaMs = current - lastMs;
		numMs++;
		totalMs += deltaMs;
		
		lastMs = current;
		
		avgMs = totalMs / numMs;
	}
	
	public double getAverageFrameTime() {
		return avgMs;
	}
	
	public long getLastFrameTime() {
		return deltaMs;
	}
	
	public int getTargetFramerate() {
		return targetFramerate;
	}

	public double getLastFrameError() {
		return deltaMs / msPerFrame;
	}
}
