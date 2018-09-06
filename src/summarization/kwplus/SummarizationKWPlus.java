package summarization.kwplus;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import bean.WeightedSet;
import main.SummarizationKWPlusMain;
import util.Parameter;

public class SummarizationKWPlus {
	BufferedReader br;
	BufferedWriter wr;
	
	SummarizationHistogramBuffered hist = new SummarizationHistogramBuffered();
	ArrayList<WeightedSet> buffer = new ArrayList<>();
	
	static long CPU_TIME = 0, POST_PROCESSING_TIME = 0;
	private static int OUTPUT_INTERVAL = Parameter.L;
	
	public void streaming() {
		try {
			this.br = new BufferedReader(new FileReader(SummarizationKWPlusMain.DATASET));
			this.wr = new BufferedWriter(new FileWriter(SummarizationKWPlusMain.RESULT));
			String line;
			while ((line = br.readLine()) != null) {
				
				WeightedSet cur = new WeightedSet(line);
				
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
					this.output(cur.id, this.wr);
					
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
		long t1 = System.nanoTime();
		SummarizationInstanceBuffered inst = this.hist.post_processing();
		long t2 = System.nanoTime();
		POST_PROCESSING_TIME = (t2 - t1);
		
		wr.write(id + "," + String.format("%.2f", inst.function_value + 1.0e-6) + "," + CPU_TIME + "," + POST_PROCESSING_TIME + "," + this.hist.statNumElements() + "," + this.hist.index.size() + "\n");
		wr.flush();
	}
}
