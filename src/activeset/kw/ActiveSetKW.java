package activeset.kw;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import bean.UserVisit;
import main.ActiveSetKWMain;
import util.Parameter;

public class ActiveSetKW {
    private ActiveSetHistogram hist = new ActiveSetHistogram();
	
    private static long CPU_TIME = 0, POST_CPU_TIME = 0;
	private static int OUTPUT_INTERVAL = Parameter.L;
	
	private ArrayList<UserVisit> buffer = new ArrayList<>();
	static LinkedList<UserVisit> WINDOW = new LinkedList<>();
	
	public void streaming() {
		try {
            BufferedReader br = new BufferedReader(new FileReader(ActiveSetKWMain.DATASET));
            BufferedWriter bw = new BufferedWriter(new FileWriter(ActiveSetKWMain.RESULT));
			String line;
			while ((line = br.readLine()) != null) {
				UserVisit cur = new UserVisit(line);
				
				if (WINDOW.size() < Parameter.W) {
					WINDOW.addLast(cur);
				} else {
					WINDOW.removeFirst();
					WINDOW.addLast(cur);
				}
				
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
					POST_CPU_TIME = this.hist.postprocessing();
					
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
	
	private void output(int id, BufferedWriter bw) throws IOException{
		bw.write(id + "," + this.hist.getResultValue() + "," + CPU_TIME + "," + POST_CPU_TIME + "\n");
	}
}
