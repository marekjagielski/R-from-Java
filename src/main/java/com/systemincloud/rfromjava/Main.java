package com.systemincloud.rfromjava;

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

public class Main {

	public static String R_PATH = "/usr/local/lib/R";

	public static void main(String[] args) {
		System.out.println("START");
		try {
			final RServiNodeConfig rConfig = new RServiNodeConfig();
			rConfig.setRHome(R_PATH);
			String policyFile = Main.class.getResource("java.policy").getFile();
			rConfig.setJavaArgs(rConfig.getJavaArgs() + " " + "-Djava.security.policy=file://" + policyFile);
			rConfig.setJavaArgs(rConfig.getJavaArgs() + " " + "-Djava.security.manager ");
			rConfig.setJavaArgs(rConfig.getJavaArgs() + " " + "-Dde.walware.rj.rpkg.path=/home/marek/R/x86_64-pc-linux-gnu-library/3.2/rj");

			rConfig.addToClasspath(NodeController.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
			rConfig.addToClasspath(AbstractServerControl.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
			rConfig.addToClasspath(RJIOExternalizable.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());



//			List<String> site = new LinkedList<>();
//			List<String> libs = new LinkedList<>();
//			List<String> libsusr = new LinkedList<>();
//
//			setLibs(site, rConfig, "R_LIBS_SITE");
//			setLibs(libs, rConfig, "R_LIBS");
//			setLibs(libsusr, rConfig, "R_LIBS_USER");
//
//	        ClassLoader sysClassLoader = ClassLoader.getSystemClassLoader();
//	        URL[] urls = ((URLClassLoader) sysClassLoader).getURLs();
//	        for(URL u : urls) rConfig.addToClasspath(u.getFile());
//	        rConfig.setBaseWorkingDirectory("/home/marek/Projects/systemInCloudModeler/runtime-EclipseApplication/xxx/target");
//
//			rConfig.setJavaArgs(rConfig.getJavaArgs() + " " + "-Djava.rmi.server.hostname=localhost ");
//
//			rConfig.setJavaArgs(rConfig.getJavaArgs() + " " + "-Drjava.class.path=" + "/home/marek/Projects/systemInCloudModeler/eclipse/plugins/de.walware.rj.server_2.0.4.b201511061600E44sw.jar");
//			rConfig.setJavaArgs(rConfig.getJavaArgs() + " " + "-Drjava.path=" + "/home/marek/R/x86_64-pc-linux-gnu-library/3.2/rjava");
//			rConfig.setJavaArgs(rConfig.getJavaArgs() + " " + "-Djava.rmi.server.codebase=" + "file:///home/marek/R/x86_64-pc-linux-gnu-library/3.2/rjava/java");
//			rConfig.setJavaArgs(rConfig.getJavaArgs() + " " + "-Djava.rmi.server.useCodebaseOnly=true");
//			rConfig.setJavaArgs(rConfig.getJavaArgs() + " " + "-Drjava.rjavalibs=" + "");
//			rConfig.setJavaArgs(rConfig.getJavaArgs() + " " + "-Drjava.jrilibs=" + "/home/marek/R/x86_64-pc-linux-gnu-library/3.2/rJava/jri");
//			System.out.println(rConfig.getJavaArgs());
//			rConfig.setEnableVerbose(true);
//			System.getenv().put("R_LIBS_USER", "/home/marek/R/x86_64-pc-linux-gnu-library/3.2");
//			this.r_arch = getNonEmpty(System.getenv("R_ARCH"), System.getProperty("r.arch"));
//			this.r_libs_site = checkDirPathList(System.getenv("R_LIBS_SITE"));
//			this.r_libs_user = checkDirPathList(System.getenv("R_LIBS_USER"));
//			this.r_libs = checkDirPathList(System.getenv("R_LIBS"));
//  		System.setSecurityManager(new SecurityManager());

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
			RServi fRservi = RServiUtil.getRServi(newEmbeddedR, "xx-test");

			double rValue = 1001;
			int nValue = 100;

			FunctionCall massCall = fRservi.createFunctionCall("library");
			massCall.add("package", "MASS");
			massCall.evalVoid(null);
			fRservi.evalVoid("mu <- c(0,0)", null);
			fRservi.evalVoid("r <- " + (rValue-1001)/1001, null);
			fRservi.evalVoid("n <- " + nValue, null);

			fRservi.close();
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
		System.out.println("END");
	}

}
