package activeset.kwplus;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import bean.UserVisit;
import main.ActiveSetKWPlusMain;
import util.Parameter;

public class ActiveSetKWPlus {
	private ActiveSetHistogramBuffered hist = new ActiveSetHistogramBuffered();
	private static long CPU_TIME = 0, POST_PROCESSING_TIME = 0;
	private static int OUTPUT_INTERVAL = Parameter.L;
	private ArrayList<UserVisit> buffer = new ArrayList<>();
	
	public void streaming() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(ActiveSetKWPlusMain.DATASET));
			BufferedWriter bw = new BufferedWriter(new FileWriter(ActiveSetKWPlusMain.RESULT));
			String line;
			while ((line = br.readLine()) != null) {
				UserVisit cur = new UserVisit(line);
				if (cur.id % Parameter.T != 0) {
					this.buffer.add(cur);
					continue;
				}
				this.buffer.add(cur);
				long t1 = System.nanoTime();
				this.hist.batchedInsert(this.buffer);
				long t2 = System.nanoTime();
				CPU_TIME = (t2 - t1);
				
				this.buffer.clear();
				if (cur.id >= Parameter.W && (cur.id - Parameter.W) % OUTPUT_INTERVAL == 0) {
					this.output(cur.id, bw);
					bw.flush();
					System.out.println(cur.id);
				}
			}
			br.close();
			bw.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void output(int id, BufferedWriter bw) throws IOException{
		long t1 = System.nanoTime();
		ActiveSetInstanceBuffered inst = this.hist.post_processing();
		long t2 = System.nanoTime();
		POST_PROCESSING_TIME = (t2 - t1);
		
		bw.write(id + "," + inst.function_value + "," + CPU_TIME + "," + POST_PROCESSING_TIME + "," + this.hist.statNumElements() + "," + this.hist.index.size() + "\n");
	}
}
