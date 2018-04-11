import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.Scanner;
import java.util.LinkedList;
import java.lang.Integer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EtchedBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import javafx.scene.CacheHint;

import java.util.Random;

public class CCacheSim extends JFrame implements ActionListener {

	private JPanel panelTop, panelLeft, panelRight, panelBottom;
	private JButton execStepBtn, execAllBtn, fileBotton,execResetBtn;
	private JComboBox<String> csBox, icsBox, dcsBox, bsBox, wayBox, replaceBox, prefetchBox, writeBox, allocBox;
	private JFileChooser fileChoose;

	private JLabel labelTop, labelLeft, rightLabel, bottomLabel, fileLabel, fileAddrBtn, stepLabel1, stepLabel2,
			csLabel, icsLabel, dcsLabel, emptyLabel, bsLabel, wayLabel, replaceLabel, prefetchLabel, writeLabel,
			allocLabel;
	private JLabel results[][], rData[][];
	private JRadioButton unifiedcacheButton, separatecacheButton;
	private int readDataMissTime, readInstMissTime, readDataHitTime, readInstHitTime, writeDataHitTime,
			writeDataMissTime, memoryWriteTime;

	//参数定义
	private String cachesize[] = { "2KB", "8KB", "32KB", "128KB", "512KB", "2MB" };
	private String spcachesize[] = { "1KB", "4KB", "16KB", "64KB", "256KB", "1MB" };
	private String blocksize[] = { "16B", "32B", "64B", "128B", "256B" };
	private String way[] = { "直接映象", "2路", "4路", "8路", "16路", "32路" };
	private String replace[] = { "LRU", "FIFO", "RAND" };
	private String pref[] = { "不预取", "不命中预取" };
	private String write[] = { "写回法", "写直达法" };
	private String alloc[] = { "按写分配", "不按写分配" };
	private String typename[] = { "读数据", "写数据", "读指令" };
	private String hitname[] = { "不命中", "命中" };

	//右侧结果显示
	private String rightTags[][] = { { "访问总次数:", "不命中次数:", "不命中率:" }, { "读指令次数:", "不命中次数:", "不命中率:" },
			{ "读数据次数:", "不命中次数:", "不命中率:" }, { "写数据次数:", "不命中次数:", "不命中率:" } };

	private String rightData[][] = { { "0 ", "0", "0.00" }, { "0", "0", "0.00" }, { "0", "0", "0.00" },
			{ "0", "0", "0.00" } };
	//打开文件
	private File file;

	//分别表示左侧几个下拉框所选择的第几项，索引从 0 开始
	private int csIndex, icsIndex, dcsIndex, bsIndex, wayIndex, replaceIndex, prefetchIndex, writeIndex, allocIndex;

	//其它变量定义
	//...
	private int CacheMod = 0;
	private int instrNum = 0;
	private int ip = 0;
	private static final int Instr_len = 32;
	private final int MAXN=1000010;
	private Instr instr_Set[];
	private Cache ucache, icache, dcache;

	private class CacheBlock {
		int tag;
		boolean dirty;
		int firsttime;
		int lasttime;
		boolean valid;
		int count;

		public CacheBlock() {
			this.tag = -1;
			dirty = false;
			firsttime = 0;
			lasttime = 0;
			valid = false;
			count = 0;
		}
	}

	private class Cache {

		private int blocksize;
		private int cachesize;
		private int blockNum;
		private int offsetlen;
		private int associativity;
		private int groupNum;
		private int indexlen;
		private CacheBlock cache[][];
		private LinkedList<CacheBlock> fifo = new LinkedList<CacheBlock>();

		public Cache(int cachesize, int blocksize) {
			this.cachesize = cachesize;
			this.blocksize = blocksize;
			blockNum = cachesize / blocksize;
			offsetlen = log(blocksize, 2);
			associativity = (int) Math.pow(2, wayIndex);
			groupNum = blockNum / associativity;
			indexlen = log(groupNum, 2);
			cache = new CacheBlock[groupNum][associativity];
			for (int i = 0; i < groupNum; i++) {
				for (int j = 0; j < associativity; j++) {
					cache[i][j] = new CacheBlock();
				}
			}
		}

		public int log(int x, int base) {
			return (int) (Math.log(x) / Math.log(base));
		}

		public boolean read(int tag, int index, int time) {
			for (int i = 0; i < associativity; i++) {
				if (cache[index][i].valid == true && cache[index][i].tag == tag) {
					cache[index][i].lasttime = time;
					return true;
				}
			}
			return false;
		}

		public boolean write(int tag, int index, int time) {
			for (int i = 0; i < associativity; i++) {
				if (cache[index][i].valid == true && cache[index][i].tag == tag) {//hit					

					cache[index][i].lasttime = time;
					if (writeIndex == 0) {
						cache[index][i].dirty = true;
					} else if (writeIndex == 1) {
					}

					return true;
				}
			}
			return false;
		}

		public void replace(int tag, int index, int time) {
			if (replaceIndex == 0) {//LRU
				int addr = 0;
				for (int i = 1; i < associativity; i++) {
					//if(cache[index][i].valid==false){
					//	continue;
					//}
					if (cache[index][addr].lasttime > cache[index][i].lasttime) {
						addr = i;

					}
				}
				load(tag, index, addr, time);
			} else if (replaceIndex == 1) {//FIFO
				int addr = 0;
				for (int i = 1; i < associativity; i++) {
					if (cache[index][addr].firsttime > cache[index][i].firsttime) {
						addr = i;
					}
				}
				load(tag, index, addr, time);
			} else if (replaceIndex == 2) {//random
				Random rand = new Random();
				int addr = rand.nextInt(associativity);
				load(tag, index, addr, time);
			}
		}

		public void load(int tag, int index, int addr, int time) {
			if (cache[index][addr].dirty == true) {
				memoryWriteTime++;
			}
			cache[index][addr].firsttime = time;
			cache[index][addr].tag = tag;
			cache[index][addr].lasttime = time;
			cache[index][addr].dirty = false;
			cache[index][addr].valid = true;
		}

	}

	private class Instr {
		protected int type;
		protected int tag, index, offset, blockaddr;
		protected String address;

		public Instr(int type, String address) {
			this.type = type;
			this.address = address;
			String s = this.transfer2radix();
			if (CacheMod == 0) {
				this.blockaddr = Integer.parseInt(s, 0, Instr_len - ucache.offsetlen, 2);
				this.tag = Integer.parseInt(s, 0, Instr_len - ucache.offsetlen - ucache.indexlen, 2);
				this.index = Integer.parseInt(s, Instr_len - ucache.offsetlen - ucache.indexlen,
						Instr_len - ucache.offsetlen, 2);
				this.offset = Integer.parseInt(s, Instr_len - ucache.offsetlen, Instr_len, 2);
			}
			if (CacheMod == 1) {
				if (type == 0 || type == 1) {
					this.tag = Integer.parseInt(s, 0, Instr_len - dcache.offsetlen - dcache.indexlen, 2);
					this.index = Integer.parseInt(s, Instr_len - dcache.offsetlen - dcache.indexlen,
							Instr_len - dcache.offsetlen, 2);
					this.offset = Integer.parseInt(s, Instr_len - dcache.offsetlen, Instr_len, 2);
				} else if (type == 2) {
					this.tag = Integer.parseInt(s, 0, Instr_len - icache.offsetlen - icache.indexlen, 2);
					this.index = Integer.parseInt(s, Instr_len - icache.offsetlen - icache.indexlen,
							Instr_len - icache.offsetlen, 2);
					this.offset = Integer.parseInt(s, Instr_len - icache.offsetlen, Instr_len, 2);
				}
			}

		}



		private String transfer2radix() {

			return String.format("%32s", Integer.toBinaryString(Integer.parseInt(this.address, 16))).replace(' ', '0');

		}

		public String description() {
			return " = type" + type + ", tag = " + tag + ", index = " + index + ", offset = " + offset;
		}
	}

	/*
	 * 构造函数，绘制模拟器面板
	 */
	public CCacheSim() {
		super("Cache Simulator");
		ip=0;
		fileChoose = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("din file", "din", "DIN");
		fileChoose.setFileFilter(filter);
		draw();
	}

	//响应事件，共有三种事件：
	//   1. 执行到底事件
	//   2. 单步执行事件
	//   3. 文件选择事件
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == execAllBtn) {
			simExecAll();
		}
		if (e.getSource() == execStepBtn) {
			simExecStep(true);
		}
		if (e.getSource() == fileBotton) {
			int fileOver = fileChoose.showOpenDialog(null);
			if (fileOver == 0) {
				String path = fileChoose.getSelectedFile().getAbsolutePath();
				fileAddrBtn.setText(path);
				System.out.println(path);
				file = new File(path);
				
				initCache();				
				readFile();
				//System.out.println("-1");
				
			}
		}
	}

	/*
	 * 初始化 Cache 模拟器
	 */
	public void initCache() {
		readDataMissTime = 0;
		readInstMissTime = 0;
		readDataHitTime = 0;
		readInstHitTime = 0;

		writeDataHitTime = 0;
		writeDataMissTime = 0;

		memoryWriteTime = 0;
		if (CacheMod == 0) {
			int cssize=(int) Math.pow(4, csIndex);
			int bcssize=(int)Math.pow(2, bsIndex);
			ucache = new Cache(2 * 1024 * cssize, 16 *bcssize);
			icache = null;
			dcache = null;


		} else if (CacheMod == 1) {
			int icssize=(int) Math.pow(4, icsIndex);
			int dcssize=(int) Math.pow(4, dcsIndex);
			int bcssize=(int)Math.pow(2, bsIndex);
			ucache = null;
			icache = new Cache(1 * 1024 * icssize, 16 * bcssize);
			dcache = new Cache(1 * 1024 * dcssize, 16 *bcssize);
		}
	}

	/*
	 * 将指令和数据流从文件中读入
	 */
	public void readFile() {
		try {
			Scanner scan = new Scanner(file);
			instr_Set = new Instr[MAXN];
			instrNum=0;
			ip=0;
			//System.out.println("-2");
			while (scan.hasNextLine()) {
				String[] temp = scan.nextLine().split("\\s+");
				if(instrNum==0){
				System.out.println(temp[0]+"**"+temp[1]);}
				instr_Set[instrNum] = new Instr(Integer.parseInt(temp[0].trim()), temp[1].trim());
				if(instrNum==0){
					System.out.println(temp[0].trim()+"**"+temp[1].trim());
					System.out.println(instr_Set[instrNum].description());
				}
				instrNum++;

			}
			scan.close();
		} catch (Exception e) {
			System.out.println("Got a Exception：" + e.getMessage());
			e.printStackTrace();
		}

	}

	/*
	 * 模拟单步执行
	 */
	public void simExecStep(boolean isshow) {
		if(ip<instrNum){
			int type = instr_Set[ip].type;
			int index = instr_Set[ip].index;
			int tag = instr_Set[ip].tag;
	
			System.out.println(instr_Set[ip].description());
	
			boolean isHit = false;
			if (CacheMod == 0) {
				/*	
					unified cache
				*/
				if (type == 0) {// read data
					isHit = ucache.read(tag, index, ip+1);
					if (isHit) {
						readDataHitTime++;
					} else {
						readDataMissTime++;
						ucache.replace(tag, index,ip+1);
					}
				} else if (type == 1) {
					isHit = ucache.write(tag, index, ip+1);
					if (isHit) {
						writeDataHitTime++;
					} else {
						writeDataMissTime++;
						if (allocIndex == 0) {

							ucache.replace(tag, index,ip+1);

							ucache.write(tag, index, ip+1);
						} else if (allocIndex == 1) {

							memoryWriteTime++;
						}
					}
	
				} else if (type == 2) {// read instruction 
					isHit = ucache.read(tag, index, ip+1);
					if (isHit) {
						readInstHitTime++;
					} else {
						readInstMissTime++;
						/*
							Now pretend to find the block in memory
						*/
						ucache.replace(tag, index,ip+1);
						/*
							Now pretend to load the data in block into CPU
						*/
						if (prefetchIndex == 0) {// do not prefetch
							//doing nothing
						} else if (prefetchIndex == 1) {// prefetch if instruction missed!
							//ucache.prefetch(instr_Set[ip].blockAddr + 1);
						}
					}
				}
	
			} else if (CacheMod == 1) {
				if (type == 0) {// read data
					isHit = dcache.read(tag, index, ip+1);
					if (isHit) {
						readDataHitTime++;
					} else {
						readDataMissTime++;
						dcache.replace(tag, index,ip+1);
					}
				} else if (type == 1) {// write data
					isHit = dcache.write(tag, index,ip+1);
					if (isHit) {
						writeDataHitTime++;
					} else {
						writeDataMissTime++;
						if (allocIndex == 0) {
							dcache.replace(tag, index,ip+1);
							dcache.write(tag, index,ip+1);
						} else if (allocIndex == 1) {
							memoryWriteTime++;
						}
					}
	
				} else if (type == 2) {
					isHit = icache.read(tag, index, ip+1);
					if (isHit) {
						readInstHitTime++;
					} else {
						readInstMissTime++;
						icache.replace(tag, index,ip+1);
						if (prefetchIndex == 0) {
						} else if (prefetchIndex == 1) {
							//icache.prefetch(instr_Set[ip].blockAddr + 1);
						}
					}
				}
			}
	
			if (isshow || ip == instrNum - 1) {
				loadresult();
			}
			ip++;
		}


	}

	private void loadresult() {

		int totalMissTime = readInstMissTime + readDataMissTime + writeDataMissTime;
		int totalVisitTime = totalMissTime + readInstHitTime + readDataHitTime + writeDataHitTime;

		rData[0][0].setText(totalVisitTime + "");
		rData[0][1].setText(totalMissTime + "");
		if (totalVisitTime > 0) {
			double missRate = ((double) totalMissTime / (double) totalVisitTime) * 100;
			rData[0][2].setText(String.format("%.2f", missRate) + "%");
		}

		rData[1][0].setText((readInstHitTime + readInstMissTime) + "");
		rData[1][1].setText(readInstMissTime + "");
		if (readInstMissTime + readInstHitTime > 0) {
			double missRate = ((double) readInstMissTime / (double) (readInstMissTime + readInstHitTime)) * 100;
			rData[1][2].setText(String.format("%.2f", missRate) + "%");
		}

		rData[2][0].setText((readDataHitTime + readDataMissTime) + "");
		rData[2][1].setText(readDataMissTime + "");
		if (readDataMissTime + readDataHitTime > 0) {
			double missRate = ((double) readDataMissTime / (double) (readDataMissTime + readDataHitTime)) * 100;
			rData[2][2].setText(String.format("%.2f", missRate) + "%");
		}

		rData[3][0].setText((writeDataHitTime + writeDataMissTime) + "");
		rData[3][1].setText(writeDataMissTime + "");
		if (writeDataMissTime + writeDataHitTime > 0) {
			double missRate = ((double) writeDataMissTime / (double) (writeDataMissTime + writeDataHitTime)) * 100;
			rData[3][2].setText(String.format("%.2f", missRate) + "%");
		}

	}


	/*
	 * 模拟执行到底
	 */
	public void simExecAll() {
		for (int i = 0; i < instrNum; i++) {
			simExecStep(false);
		}

	}

	public static void main(String[] args) {
		new CCacheSim();
	}

	private void cacheTypeSelect(int CacheMod) {
		if (CacheMod == 0) {
			unifiedcacheButton.setSelected(true);
			csBox.setEnabled(true);
			csLabel.setEnabled(true);
			separatecacheButton.setSelected(false);
			icsBox.setEnabled(false);
			icsLabel.setEnabled(false);
			dcsLabel.setEnabled(false);
			dcsBox.setEnabled(false);

		} else if (CacheMod == 1) {
			separatecacheButton.setSelected(true);
			icsBox.setEnabled(true);
			icsLabel.setEnabled(true);
			dcsLabel.setEnabled(true);
			dcsBox.setEnabled(true);
			unifiedcacheButton.setSelected(false);
			csBox.setEnabled(false);
			csLabel.setEnabled(false);
		} else {
			System.out.println("Something error happened in CacheMod:" + CacheMod);
			System.exit(-1);
		}
	}

	/**
	 * 绘制 Cache 模拟器图形化界面
	 * 无需做修改
	 */
	public void draw() {
		//模拟器绘制面板
		setLayout(new BorderLayout(5, 5));
		panelTop = new JPanel();
		panelLeft = new JPanel();
		panelRight = new JPanel();
		panelBottom = new JPanel();
		panelTop.setPreferredSize(new Dimension(800, 50));
		panelLeft.setPreferredSize(new Dimension(300, 450));
		panelRight.setPreferredSize(new Dimension(500, 450));
		panelBottom.setPreferredSize(new Dimension(800, 100));
		panelTop.setBorder(new EtchedBorder(EtchedBorder.RAISED));
		panelLeft.setBorder(new EtchedBorder(EtchedBorder.RAISED));
		panelRight.setBorder(new EtchedBorder(EtchedBorder.RAISED));
		panelBottom.setBorder(new EtchedBorder(EtchedBorder.RAISED));

		//*****************************顶部面板绘制*****************************************//
		labelTop = new JLabel("Cache Simulator");
		labelTop.setAlignmentX(CENTER_ALIGNMENT);
		panelTop.add(labelTop);

		//*****************************左侧面板绘制*****************************************//
		labelLeft = new JLabel("Cache 参数设置");
		labelLeft.setPreferredSize(new Dimension(300, 40));

		//cache 种类
		unifiedcacheButton = new JRadioButton("UnifiedCache:", true);
		unifiedcacheButton.setPreferredSize(new Dimension(120, 30));
		unifiedcacheButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				CacheMod = 0;
				cacheTypeSelect(CacheMod);
			}
		});

		//cache 大小设置
		csLabel = new JLabel("总大小");
		csLabel.setPreferredSize(new Dimension(60, 30));
		csBox = new JComboBox<String>(cachesize);
		csBox.setPreferredSize(new Dimension(90, 30));
		csBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				csIndex = csBox.getSelectedIndex();
			}
		});

		separatecacheButton = new JRadioButton("SeprateCache:");
		separatecacheButton.setPreferredSize(new Dimension(120, 30));
		separatecacheButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				CacheMod = 1;
				cacheTypeSelect(CacheMod);
			}
		});

		icsLabel = new JLabel("ICache");
		icsLabel.setPreferredSize(new Dimension(60, 30));

		icsBox = new JComboBox<String>(spcachesize);
		icsBox.setPreferredSize(new Dimension(90, 30));
		icsBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				icsIndex = icsBox.getSelectedIndex();
			}
		});

		emptyLabel = new JLabel("");
		emptyLabel.setPreferredSize(new Dimension(120, 30));

		dcsLabel = new JLabel("DCache");
		dcsLabel.setPreferredSize(new Dimension(60, 30));
		dcsBox = new JComboBox<String>(spcachesize);
		dcsBox.setPreferredSize(new Dimension(90, 30));
		dcsBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				dcsIndex = dcsBox.getSelectedIndex();
			}
		});

		cacheTypeSelect(0);
		//cache 块大小设置
		bsLabel = new JLabel("块大小");
		bsLabel.setPreferredSize(new Dimension(120, 30));
		bsBox = new JComboBox<String>(blocksize);
		bsBox.setPreferredSize(new Dimension(160, 30));
		bsBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				bsIndex = bsBox.getSelectedIndex();
			}
		});

		//相连度设置
		wayLabel = new JLabel("相联度");
		wayLabel.setPreferredSize(new Dimension(120, 30));
		wayBox = new JComboBox<String>(way);
		wayBox.setPreferredSize(new Dimension(160, 30));
		wayBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				wayIndex = wayBox.getSelectedIndex();
			}
		});

		//替换策略设置
		replaceLabel = new JLabel("替换策略");
		replaceLabel.setPreferredSize(new Dimension(120, 30));
		replaceBox = new JComboBox<String>(replace);
		replaceBox.setPreferredSize(new Dimension(160, 30));
		replaceBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				replaceIndex = replaceBox.getSelectedIndex();
			}
		});

		//欲取策略设置
		prefetchLabel = new JLabel("预取策略");
		prefetchLabel.setPreferredSize(new Dimension(120, 30));
		prefetchBox = new JComboBox<String>(pref);
		prefetchBox.setPreferredSize(new Dimension(160, 30));
		prefetchBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				prefetchIndex = prefetchBox.getSelectedIndex();
			}
		});

		//写策略设置
		writeLabel = new JLabel("写策略");
		writeLabel.setPreferredSize(new Dimension(120, 30));
		writeBox = new JComboBox<String>(write);
		writeBox.setPreferredSize(new Dimension(160, 30));
		writeBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				writeIndex = writeBox.getSelectedIndex();
			}
		});

		//调块策略
		allocLabel = new JLabel("写不命中调块策略");
		allocLabel.setPreferredSize(new Dimension(120, 30));
		allocBox = new JComboBox<String>(alloc);
		allocBox.setPreferredSize(new Dimension(160, 30));
		allocBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				allocIndex = allocBox.getSelectedIndex();
			}
		});

		//选择指令流文件
		fileLabel = new JLabel("选择指令流文件");
		fileLabel.setPreferredSize(new Dimension(120, 30));
		fileAddrBtn = new JLabel();
		fileAddrBtn.setPreferredSize(new Dimension(210, 30));
		fileAddrBtn.setBorder(new EtchedBorder(EtchedBorder.RAISED));
		fileBotton = new JButton("浏览");
		fileBotton.setPreferredSize(new Dimension(70, 30));
		fileBotton.addActionListener(this);

		panelLeft.add(labelLeft);
		panelLeft.add(unifiedcacheButton);
		panelLeft.add(csLabel);
		panelLeft.add(csBox);
		panelLeft.add(separatecacheButton);
		panelLeft.add(icsLabel);
		panelLeft.add(icsBox);
		panelLeft.add(emptyLabel);
		panelLeft.add(dcsLabel);
		panelLeft.add(dcsBox);
		panelLeft.add(bsLabel);
		panelLeft.add(bsBox);
		panelLeft.add(wayLabel);
		panelLeft.add(wayBox);
		panelLeft.add(replaceLabel);
		panelLeft.add(replaceBox);
		panelLeft.add(prefetchLabel);
		panelLeft.add(prefetchBox);
		panelLeft.add(writeLabel);
		panelLeft.add(writeBox);
		panelLeft.add(allocLabel);
		panelLeft.add(allocBox);
		panelLeft.add(fileLabel);
		panelLeft.add(fileAddrBtn);
		panelLeft.add(fileBotton);

		//*****************************右侧面板绘制*****************************************//
		//模拟结果展示区域
		rightLabel = new JLabel("模拟结果");
		rightLabel.setPreferredSize(new Dimension(500, 40));
		results = new JLabel[4][3];
		rData =new JLabel[4][3];
		for (int i = 0; i < 4; i++) {
			for(int j=0;j<3;j++){
				results[i][j] = new JLabel(rightTags[i][j]);
				results[i][j].setPreferredSize(new Dimension(70, 40));
			}
		}
		for (int i = 0; i < 4; i++) {
			for(int j=0;j<3;j++){
				rData[i][j] = new JLabel(rightData[i][j]);
				rData[i][j].setPreferredSize(new Dimension(70, 40));
			}
		}

		panelRight.add(rightLabel);
		for (int i = 0; i < 4; i++) {
			for(int j=0;j<3;j++){
				panelRight.add(results[i][j]);
				panelRight.add(rData[i][j]);
			}

		}



		//*****************************底部面板绘制*****************************************//

		bottomLabel = new JLabel("执行控制");
		bottomLabel.setPreferredSize(new Dimension(800, 30));
		execStepBtn = new JButton("步进");
		execStepBtn.setLocation(100, 30);
		execStepBtn.addActionListener(this);
		execAllBtn = new JButton("执行到底");
		execAllBtn.setLocation(300, 30);
		execAllBtn.addActionListener(this);
		execResetBtn =new JButton("RESET");
		execResetBtn.setLocation(50,30);
		execResetBtn.addActionListener(new ActionListener(){
		
			@Override
			public void actionPerformed(ActionEvent e) {
				for (int i = 0; i < 4; i++) {
					for(int j=0;j<2;j++){
						rData[i][j].setText("0");
					}
					rData[i][2].setText("0.00%");
				}
				ip=0;
				fileAddrBtn.setText("");
				initCache();
			}
		});
		
		panelBottom.add(bottomLabel);
		panelBottom.add(execResetBtn);
		panelBottom.add(execStepBtn);
		panelBottom.add(execAllBtn);

		add("North", panelTop);
		add("West", panelLeft);
		add("Center", panelRight);
		add("South", panelBottom);
		setSize(820, 620);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}
