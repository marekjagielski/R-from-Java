package com.systemincloud.rfromjava;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;

import de.walware.ecommons.ECommons;
import de.walware.ecommons.IDisposable;
import de.walware.ecommons.net.RMIRegistry;
import de.walware.ecommons.net.RMIUtil;
import de.walware.rj.data.RJIOExternalizable;
import de.walware.rj.server.srvImpl.AbstractServerControl;
import de.walware.rj.server.srvext.ERJContext;
import de.walware.rj.servi.RServi;
import de.walware.rj.servi.RServiUtil;
import de.walware.rj.servi.internal.NodeController;
import de.walware.rj.servi.pool.EmbeddedRServiManager;
import de.walware.rj.servi.pool.RServiImplE;
import de.walware.rj.servi.pool.RServiNodeConfig;
import de.walware.rj.servi.pool.RServiNodeFactory;
import de.walware.rj.services.FunctionCall;
import de.walware.rj.services.utils.Graphic;
import de.walware.rj.services.utils.PngGraphic;

public class Main extends JFrame {

	private static final long serialVersionUID = 1L;

	public static String R_PATH  = "/usr/local/lib/R";
	public static String RJ_PATH = "/home/marek/R/x86_64-pc-linux-gnu-library/3.2/rj";

	private RServi fRservi;

	private JPanel mainPane;
	private JPanel gPanel;

	private static int w = 300;
	private static int h = 300;

	public static void main(String[] args) {
		System.out.println("START");
		new Main().run();
		System.out.println("END");
	}

	private void run() {
		setSize(w, h);
		mainPane = new JPanel();
		mainPane.setPreferredSize(new Dimension(w, h));
		mainPane.setLayout(new BorderLayout(0, 0));
		setContentPane(mainPane);
		r();
		this.setVisible(true);
	}

	private void r() {
		try {
			RServiNodeConfig rConfig = new RServiNodeConfig();
			rConfig.setRHome(R_PATH);
			String policyFile = Main.class.getResource("java.policy").getFile();
			rConfig.setJavaArgs(rConfig.getJavaArgs() + " " + "-Djava.security.policy=file://" + policyFile);
			rConfig.setJavaArgs(rConfig.getJavaArgs() + " " + "-Djava.security.manager");
			rConfig.setJavaArgs(rConfig.getJavaArgs() + " " + "-Dde.walware.rj.rpkg.path=" + RJ_PATH);
			rConfig.setJavaArgs(rConfig.getJavaArgs() + " " + "-Dde.walware.rj.debug");

			rConfig.addToClasspath(NodeController.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
			rConfig.addToClasspath(AbstractServerControl.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
			rConfig.addToClasspath(RJIOExternalizable.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());

			ERJContext context = new ERJContext();
			ECommons.init("dummy_id", new ECommons.IAppEnvironment() {
				@Override public void removeStoppingListener(IDisposable arg0) { System.out.println("removeStoppingListener"); }
				@Override public void log(IStatus arg0)                        { System.out.println("log"); }
				@Override public void addStoppingListener(IDisposable arg0)    { System.out.println("addStoppingListener"); }
			});
			RMIUtil.INSTANCE.setEmbeddedPrivateMode(false);
			RMIRegistry registry = RMIUtil.INSTANCE.getEmbeddedPrivateRegistry(new NullProgressMonitor());
			rConfig.setNodeArgs(rConfig.getNodeArgs() + " -embedded");

			RServiNodeFactory nodeFactory = RServiImplE.createLocalNodeFactory("pool", context);
			nodeFactory.setRegistry(registry);
			nodeFactory.setConfig(rConfig);

			EmbeddedRServiManager newEmbeddedR = RServiImplE.createEmbeddedRServi("pool", registry, nodeFactory);
			this.fRservi = RServiUtil.getRServi(newEmbeddedR, "xx-test");

			this.fRservi.evalVoid("cat(\"Hello from R\")", null);

			double rValue = 1001;
			int nValue = 100;

			FunctionCall massCall = this.fRservi.createFunctionCall("library");
			massCall.add("package", "MASS");
			massCall.evalVoid(null);
			this.fRservi.evalVoid("mu <- c(0,0)", null);
			this.fRservi.evalVoid("r <- " + (rValue-1001)/1001, null);
			this.fRservi.evalVoid("n <- " + nValue, null);
			makePlot();
			this.fRservi.close();
			newEmbeddedR.stop();
		} catch (Exception e1) {
			e1.printStackTrace();

//			StackTraceElement[] st = e1.getStackTrace();
//			System.out.println(e1.getMessage());
//			System.out.println(StringUtils.join(st, "\n"));
//			Throwable e2 = e1.getCause();
//			st = e2.getStackTrace();
//			System.out.println(e2.getMessage());
//			System.out.println(StringUtils.join(st, "\n"));
//			Throwable e3 = e2.getCause();
//			st = e3.getStackTrace();
//			System.out.println(e3.getMessage());
//			System.out.println(StringUtils.join(st, "\n"));
//			e1.printStackTrace();
		}
	}

	public void makePlot(){
		try {
			FunctionCall mvrnormCall = this.fRservi.createFunctionCall("mvrnorm");
			mvrnormCall.add("n", "n");
			mvrnormCall.add("mu", "mu");

			this.fRservi.evalVoid("sigma <- matrix(rep(r, 4), ncol = 2)", null);
			this.fRservi.evalVoid("diag(sigma) <- c(1,1)", null);
			this.fRservi.evalVoid("xy <- mvrnorm(n = n, mu = mu, Sigma = sigma)", null);

			// plot(x = xy[,1], y = xy[,2], ylab = "", xlab = "")

			PngGraphic pngGraphic = new PngGraphic();
			pngGraphic.setSize(w, h, Graphic.UNIT_PX);

			FunctionCall plotFun = fRservi.createFunctionCall("eqscplot");
			plotFun.add("x", "xy[,1]");
			plotFun.add("y", "xy[,2]");
			plotFun.addChar("xlab", "");
			plotFun.addChar("ylab", "");

			byte[] plot = pngGraphic.create(plotFun, fRservi, null);
			final BufferedImage img = ImageIO.read(new ByteArrayInputStream(plot));

			gPanel = new JPanel() { private static final long serialVersionUID = 1L;
				@Override public void paintComponent(Graphics g) {
					super.paintComponent(g);
						g.drawImage(img, 0, 0, null);
				}
			};
			mainPane.add(gPanel, BorderLayout.CENTER);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
