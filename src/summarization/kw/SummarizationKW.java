package summarization.kw;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import bean.WeightedSet;
import main.SummarizationKWMain;
import util.Parameter;

public class SummarizationKW {
	BufferedReader br;
	BufferedWriter wr;
	
	SummarizationHistogram hist = new SummarizationHistogram();
	ArrayList<WeightedSet> buffer = new ArrayList<>();
	
	static int OUTPUT_INTERVAL = Parameter.L;
	static long CPU_TIME = 0, POST_CPU_TIME = 0;
	static LinkedList<WeightedSet> WINDOW = new LinkedList<>();
	
	public void streaming() {
		try {
			this.br = new BufferedReader(new FileReader(SummarizationKWMain.DATASET));
			this.wr = new BufferedWriter(new FileWriter(SummarizationKWMain.RESULT));
			String line;
			while ((line = br.readLine()) != null) {
				WeightedSet cur = new WeightedSet(line);
				
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
					this.output(cur.id, this.wr);
					this.wr.flush();
					System.out.println(cur.id);
				}
			}
			this.br.close();
			this.wr.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void output(int id, BufferedWriter wr) throws IOException {
		wr.write(id + "," + String.format("%.2f", this.hist.getResultValue() + 1.0e-6) + "," + CPU_TIME + "," + POST_CPU_TIME + "\n");
		wr.flush();
	}
}
