import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.lang.*;
import java.util.*;

public class MyCacheSim extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	/*
		ui property
	*/
	private JPanel panelTop, panelLeft, panelRight, panelBottom;
	private JButton execStepBtn, execAllBtn, fileBotton;
	private JComboBox<String> csBox, bsBox, wayBox, replaceBox, prefetchBox, writeBox, allocBox;
	private JComboBox<String> icsBox, dcsBox;
	private JFileChooser fileChooser;

	private JLabel labelTop, labelLeft, rightLabel, bottomLabel, fileLabel, fileAddrBtn, stepLabel1, stepLabel2,
			csLabel, bsLabel, wayLabel, replaceLabel, prefetchLabel, writeLabel, allocLabel;
	private JLabel icsLabel, dcsLabel;
	private JLabel resultTagLabel[][];
	private JLabel resultDataLabel[][];

	private JLabel accessTypeTagLabel, addressTagLabel, blockNumberTagLabel, tagTagLabel, indexTagLabel,
			inblockAddressTagLabel, hitTagLabel;
	private JLabel accessTypeDataLabel, addressDataLabel, blockNumberDataLabel, tagDataLabel, indexDataLabel,
			inblockAddressDataLabel, hitDataLabel;

	private JRadioButton unifiedCacheButton, separateCacheButton;
	/*
		options section
	*/
	private final String cachesize[] = { "2KB", "8KB", "32KB", "128KB", "512KB", "2MB" };
	private final String scachesize[] = { "1KB", "4KB", "16KB", "64KB", "256KB", "1MB" };
	private final String blocksize[] = { "16B", "32B", "64B", "128B", "256B" };
	private final String way[] = { "直接映象", "2路", "4路", "8路", "16路", "32路" };
	private final String replace[] = { "LRU", "FIFO", "RAND" };
	private final String pref[] = { "不预取", "不命中预取" };
	private final String write[] = { "写回法", "写直达法" };
	private final String alloc[] = { "按写分配", "不按写分配" };
	//private final  String typename[] = { "读数据", "写数据", "读指令" };
	//private String hitname[] = {"不命中", "命中" };

	private final String resultTags[][] = { { "访问总次数:", "不命中次数:", "不命中率:" }, { "读指令次数:", "不命中次数:", "不命中率:" },
			{ "读数据次数:", "不命中次数:", "不命中率:" }, { "写数据次数:", "不命中次数:", "不命中率:" } };

	/*
		loading file
	*/
	private File file;

	/*
		user options record
	*/
	private int csIndex, bsIndex, wayIndex, replaceIndex, prefetchIndex, writeIndex, allocIndex;

	private int mcsIndex, mbsIndex, mwayIndex, mreplaceIndex, mprefetchIndex, mwriteIndex, mallocIndex;

	private int icsIndex, dcsIndex, micsIndex, mdcsIndex;

	private int cacheType = 0, mcacheType = 0;

	/*
		instruction class
	*/
	private class Instruction {
		int opt;
		int tag;
		int index;
		int blockAddr;
		int inblockAddr;
		String addr;

		public Instruction(int opt, String addr) {
			this.opt = opt;
			this.addr = addr;

			String baddr = this.HexAddr2BinAddr();
			//System.out.println(baddr);

			if (mcacheType == 0 && uCache != null) {
				this.tag = Integer.parseInt(baddr.substring(0, 32 - uCache.blockOffset - uCache.groupOffset), 2);
				this.index = Integer.parseInt(
						baddr.substring(32 - uCache.blockOffset - uCache.groupOffset, 32 - uCache.blockOffset), 2);
				this.blockAddr = Integer.parseInt(baddr.substring(0, 32 - uCache.blockOffset), 2);
				this.inblockAddr = Integer.parseInt(baddr.substring(32 - uCache.blockOffset), 2);
			}
			if (mcacheType == 1 && iCache != null && dCache != null) {
				if (opt == 0 || opt == 1) {
					this.tag = Integer.parseInt(baddr.substring(0, 32 - dCache.blockOffset - dCache.groupOffset), 2);
					this.index = Integer.parseInt(
							baddr.substring(32 - dCache.blockOffset - dCache.groupOffset, 32 - dCache.blockOffset), 2);
					this.blockAddr = Integer.parseInt(baddr.substring(0, 32 - dCache.blockOffset), 2);
					this.inblockAddr = Integer.parseInt(baddr.substring(32 - dCache.blockOffset), 2);
				} else if (opt == 2) {
					this.tag = Integer.parseInt(baddr.substring(0, 32 - iCache.blockOffset - iCache.groupOffset), 2);
					this.index = Integer.parseInt(
							baddr.substring(32 - iCache.blockOffset - iCache.groupOffset, 32 - iCache.blockOffset), 2);
					this.blockAddr = Integer.parseInt(baddr.substring(0, 32 - iCache.blockOffset), 2);
					this.inblockAddr = Integer.parseInt(baddr.substring(32 - iCache.blockOffset), 2);
				}
			}
		}

		public String description() {
			return "opt = " + opt + ", tag = " + tag + ", index = " + index + ", inblockAddr = " + inblockAddr;
		}

		private String HexAddr2BinAddr() {
			StringBuffer sb = new StringBuffer();
			int zero = 8 - this.addr.length();
			for (int i = 0; i < zero; i++) {
				sb.append("0000");
			}
			for (int i = 0; i < this.addr.length(); i++) {
				switch (this.addr.charAt(i)) {
				case '0':
					sb.append("0000");
					break;
				case '1':
					sb.append("0001");
					break;
				case '2':
					sb.append("0010");
					break;
				case '3':
					sb.append("0011");
					break;
				case '4':
					sb.append("0100");
					break;
				case '5':
					sb.append("0101");
					break;
				case '6':
					sb.append("0110");
					break;
				case '7':
					sb.append("0111");
					break;
				case '8':
					sb.append("1000");
					break;
				case '9':
					sb.append("1001");
					break;
				case 'a':
					sb.append("1010");
					break;
				case 'b':
					sb.append("1011");
					break;
				case 'c':
					sb.append("1100");
					break;
				case 'd':
					sb.append("1101");
					break;
				case 'e':
					sb.append("1110");
					break;
				case 'f':
					sb.append("1111");
					break;
				default:
					System.out.println("Data Error!");
				}
			}

			return sb.toString();
		}

	}

	/*
		instruction property
	*/
	private Instruction instructions[];
	private final int INSTRUCTION_MAX_SIZE = 100000;
	private int isize;
	private int ip;

	private class CacheBlock {
		int tag;
		boolean dirty;
		int count;
		long time;

		public CacheBlock(int tag) {
			this.tag = tag;
			dirty = false;
			count = 0;
			time = -1L;
		}
	}

	/*
		cache class
	*/
	private class Cache {
		/*
			cache property
		*/
		private CacheBlock cache[][];
		private int cacheSize;
		private int blockSize;
		private int blockNum;
		private int blockOffset;
		private int blockNumInAGroup;
		private int groupNum;
		private int groupOffset;

		private long groupFIFOTime[];

		public Cache(int csize, int bsize) {
			cacheSize = csize;
			blockSize = bsize;

			blockNum = cacheSize / blockSize;
			blockOffset = log2(blockSize);

			blockNumInAGroup = pow(2, mwayIndex);
			groupNum = blockNum / blockNumInAGroup;
			groupOffset = log2(groupNum);

			cache = new CacheBlock[groupNum][blockNumInAGroup];

			for (int i = 0; i < groupNum; i++) {
				for (int j = 0; j < blockNumInAGroup; j++) {
					cache[i][j] = new CacheBlock(-1);
				}
			}

			groupFIFOTime = new long[groupNum];
		}

		public boolean read(int tag, int index, int inblockAddr) {
			for (int i = 0; i < blockNumInAGroup; i++) {
				if (cache[index][i].tag == tag) {//hit
					cache[index][i].count++;
					/*
						Now pretend to send data to CPU
					*/
					return true;
				}
			}
			return false;
		}

		public boolean write(int tag, int index, int inblockAddr) {
			for (int i = 0; i < blockNumInAGroup; i++) {
				if (cache[index][i].tag == tag) {//hit					
					cache[index][i].count++;
					cache[index][i].dirty = true;
					/*
						Now pretend to write data to Cache
					*/
					if (mwriteIndex == 0) {//write back
						//doing nothing
					} else if (mwriteIndex == 1) {//write through
						memoryWriteTime++;
						/*
							pretending to write dirty cache to memory after write to cache
						*/
						cache[index][i].dirty = false;
					}

					return true;
				}
			}
			return false;
		}

		public void prefetch(int nextBlockAddr) {

			int nextTag = nextBlockAddr / pow(2, groupOffset + blockOffset);
			int nextIndex = nextBlockAddr / pow(2, blockOffset) % pow(2, groupOffset);

			replaceCacheBlock(nextTag, nextIndex);
		}

		public void replaceCacheBlock(int tag, int index) {
			if (mreplaceIndex == 0) {//LRU
				int lruBlock = 0;
				for (int i = 1; i < blockNumInAGroup; i++) {
					if (cache[index][lruBlock].count > cache[index][i].count) {
						lruBlock = i;
					}
				}
				loadToCache(tag, index, lruBlock);
			} else if (mreplaceIndex == 1) {//FIFO
				int fifoBlock = 0;
				for (int i = 1; i < blockNumInAGroup; i++) {
					if (cache[index][fifoBlock].time > cache[index][i].time) {
						fifoBlock = i;
					}
				}
				loadToCache(tag, index, fifoBlock);
			} else if (mreplaceIndex == 2) {//random
				int ranBlock = random(0, blockNumInAGroup);
				loadToCache(tag, index, ranBlock);
			}
		}

		private void loadToCache(int tag, int index, int groupAddr) {
			if (mwriteIndex == 0 && cache[index][groupAddr].dirty) {
				//write back before being replaced;
				memoryWriteTime++;
			}

			cache[index][groupAddr].tag = tag;
			cache[index][groupAddr].count = 1;
			cache[index][groupAddr].dirty = false;
			cache[index][groupAddr].time = groupFIFOTime[index];
			groupFIFOTime[index]++;
		}

		public void description() {
			System.out.println("cacheSize = " + cacheSize);
			System.out.println("blockSize = " + blockSize);
			System.out.println("blockNum = " + blockNum);
			System.out.println("blockOffset = " + blockOffset);
			System.out.println("blockNumInAGroup = " + blockNumInAGroup);
			System.out.println("groupNum = " + groupNum);
			System.out.println("groupOffset = " + groupOffset);
		}
	}

	Cache uCache, iCache, dCache;

	/*
	 *	statistic property
	 */

	private int readDataMissTime, readInstMissTime, readInstHitTime, readDataHitTime;
	private int writeDataHitTime, writeDataMissTime;
	private int memoryWriteTime;

	/*
	 *	JFileChooser Filter Class
	 */
	private class DinFileFilter extends javax.swing.filechooser.FileFilter {
		public boolean accept(File f) {
			if (f.isDirectory())
				return true;
			String name = f.getName();
			return name.endsWith(".din") || name.endsWith(".DIN");
		}

		public String getDescription() {
			return ".din";
		}
	}

	/*
	 *	cache simulator class
	 */
	public MyCacheSim() {
		super("Cache Simulator");
		fileChooser = new JFileChooser();
		fileChooser.setFileFilter(new DinFileFilter());
		draw();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == execAllBtn) {
			simExecAll();
		}
		if (e.getSource() == execStepBtn) {
			simExecStep(true);
		}
		if (e.getSource() == fileBotton) {
			int fileOver = fileChooser.showOpenDialog(null);
			if (fileOver == 0) {
				String path = fileChooser.getSelectedFile().getAbsolutePath();
				fileAddrBtn.setText(path);
				file = new File(path);

				/*
					fix the setting
				*/
				mcacheType = cacheType;
				mcsIndex = csIndex;
				micsIndex = icsIndex;
				mdcsIndex = dcsIndex;
				mbsIndex = bsIndex;
				mwayIndex = wayIndex;
				mreplaceIndex = replaceIndex;
				mprefetchIndex = prefetchIndex;
				mwriteIndex = writeIndex;
				mallocIndex = allocIndex;

				initCache();
				readFile();
				reloadUI();
			}
		}
	}

	/*
	 * 初始化 Cache 模拟器
	 */
	private void initCache() {
		/*
			reset statistic properties
		*/
		readDataMissTime = 0;
		readInstMissTime = 0;
		readDataHitTime = 0;
		readInstHitTime = 0;

		writeDataHitTime = 0;
		writeDataMissTime = 0;

		memoryWriteTime = 0;

		/*
			Cache initialization
		*/

		if (mcacheType == 0) {
			uCache = new Cache(2 * 1024 * pow(4, mcsIndex), 16 * pow(2, mbsIndex));
			iCache = null;
			dCache = null;

			System.out.println("Unified Cache:");
			uCache.description();

		} else if (mcacheType == 1) {
			uCache = null;
			iCache = new Cache(1 * 1024 * pow(4, micsIndex), 16 * pow(2, mbsIndex));
			dCache = new Cache(1 * 1024 * pow(4, mdcsIndex), 16 * pow(2, mbsIndex));

			System.out.println("Instruction Cache:");
			iCache.description();
			System.out.println("Data Cache:");
			dCache.description();
		}
	}

	/*
	 * 将指令和数据流从文件中读入
	 */
	private void readFile() {
		try {
			Scanner s = new Scanner(file);
			instructions = new Instruction[INSTRUCTION_MAX_SIZE];
			isize = 0;
			ip = 0;

			while (s.hasNextLine()) {
				String line = s.nextLine();
				String[] items = line.split(" ");
				instructions[isize] = new Instruction(Integer.parseInt(items[0].trim()), items[1].trim());
				isize++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void reloadUI() {
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 2; j++) {
				resultDataLabel[i][j].setText("0");
			}
			resultDataLabel[i][2].setText("0.00%");
		}

		accessTypeDataLabel.setText("--");
		addressDataLabel.setText("--");
		blockNumberDataLabel.setText("--");
		tagDataLabel.setText("--");
		indexDataLabel.setText("--");
		inblockAddressDataLabel.setText("--");
		hitDataLabel.setText("--");
	}

	/*
	 * 模拟单步执行
	 */
	private void simExecStep(boolean oneStepExec) {
		ip %= isize;
		if (ip == 0) {
			initCache();
			reloadUI();
		}
		int opt = instructions[ip].opt;
		int index = instructions[ip].index;
		int tag = instructions[ip].tag;
		int inblockAddr = instructions[ip].inblockAddr;

		//System.out.printf("opt = %d, tag = %d, index = %d, inblockAddr = %d\n", opt, tag, index, inblockAddr);
		System.out.println(instructions[ip].description());

		boolean isHit = false;
		if (mcacheType == 0) {
			/*	
				unified cache
			*/
			if (opt == 0) {// read data
				isHit = uCache.read(tag, index, inblockAddr);
				if (isHit) {
					readDataHitTime++;
				} else {
					readDataMissTime++;
					/*
						Now pretend to find the block in memory
					*/
					uCache.replaceCacheBlock(tag, index);
					/*
						Now pretend to load the data in block into CPU
					*/
				}
			} else if (opt == 1) {// write data
				isHit = uCache.write(tag, index, inblockAddr);
				if (isHit) {
					writeDataHitTime++;
				} else {
					writeDataMissTime++;
					/*
						Now pretend to find the block in memory
					*/
					if (mallocIndex == 0) {//write alloc
						/*
							load the target block into Cache
						*/
						uCache.replaceCacheBlock(tag, index);
						/*
							pretend to write into the loaded Cache block
						*/
						uCache.write(tag, index, inblockAddr);
					} else if (mallocIndex == 1) {//no write alloc
						/*
							do not load the written-missed block into Cache
							just pretend to write to memory
						*/
						memoryWriteTime++;
					}
				}

			} else if (opt == 2) {// read instruction 
				isHit = uCache.read(tag, index, inblockAddr);
				if (isHit) {
					readInstHitTime++;
				} else {
					readInstMissTime++;
					/*
						Now pretend to find the block in memory
					*/
					uCache.replaceCacheBlock(tag, index);
					/*
						Now pretend to load the data in block into CPU
					*/
					if (mprefetchIndex == 0) {// do not prefetch
						//doing nothing
					} else if (mprefetchIndex == 1) {// prefetch if instruction missed!
						uCache.prefetch(instructions[ip].blockAddr + 1);
					}
				}
			}

		} else if (mcacheType == 1) {
			/*
				seperated cache
			*/
			if (opt == 0) {// read data
				isHit = dCache.read(tag, index, inblockAddr);
				if (isHit) {
					readDataHitTime++;
				} else {
					readDataMissTime++;
					/*
						Now pretend to find the block in memory
					*/
					dCache.replaceCacheBlock(tag, index);
					/*
						Now pretend to load the data in block into CPU
					*/
				}
			} else if (opt == 1) {// write data
				isHit = dCache.write(tag, index, inblockAddr);
				if (isHit) {
					writeDataHitTime++;
				} else {
					writeDataMissTime++;
					/*
						Now pretend to find the block in memory
					*/
					if (mallocIndex == 0) {//write alloc
						/*
							load the target block into Cache
						*/
						dCache.replaceCacheBlock(tag, index);
						/*
							pretend to write into the loaded Cache block
						*/
						dCache.write(tag, index, inblockAddr);
					} else if (mallocIndex == 1) {//no write alloc
						/*
							do not load the written-missed block into Cache
							just pretend to write to memory
						*/
						memoryWriteTime++;
					}
				}

			} else if (opt == 2) {// read instruction 
				isHit = iCache.read(tag, index, inblockAddr);
				if (isHit) {
					readInstHitTime++;
				} else {
					readInstMissTime++;
					/*
						Now pretend to find the block in memory
					*/
					iCache.replaceCacheBlock(tag, index);
					/*
						Now pretend to load the data in block into CPU
					*/
					if (mprefetchIndex == 0) {// do not prefetch
						//doing nothing
					} else if (mprefetchIndex == 1) {// prefetch if instruction missed!
						iCache.prefetch(instructions[ip].blockAddr + 1);
					}
				}
			}
		}

		if (oneStepExec || ip == isize - 1) {
			statisticUIUpdate(instructions[ip], isHit);
		}
		ip++;
	}

	private void statisticUIUpdate(Instruction inst, boolean isHit) {

		int totalMissTime = readInstMissTime + readDataMissTime + writeDataMissTime;
		int totalVisitTime = totalMissTime + readInstHitTime + readDataHitTime + writeDataHitTime;

		resultDataLabel[0][0].setText(totalVisitTime + "");
		resultDataLabel[0][1].setText(totalMissTime + "");
		if (totalVisitTime > 0) {
			double missRate = ((double) totalMissTime / (double) totalVisitTime) * 100;
			resultDataLabel[0][2].setText(String.format("%.2f", missRate) + "%");
		}

		resultDataLabel[1][0].setText((readInstHitTime + readInstMissTime) + "");
		resultDataLabel[1][1].setText(readInstMissTime + "");
		if (readInstMissTime + readInstHitTime > 0) {
			double missRate = ((double) readInstMissTime / (double) (readInstMissTime + readInstHitTime)) * 100;
			resultDataLabel[1][2].setText(String.format("%.2f", missRate) + "%");
		}

		resultDataLabel[2][0].setText((readDataHitTime + readDataMissTime) + "");
		resultDataLabel[2][1].setText(readDataMissTime + "");
		if (readDataMissTime + readDataHitTime > 0) {
			double missRate = ((double) readDataMissTime / (double) (readDataMissTime + readDataHitTime)) * 100;
			resultDataLabel[2][2].setText(String.format("%.2f", missRate) + "%");
		}

		resultDataLabel[3][0].setText((writeDataHitTime + writeDataMissTime) + "");
		resultDataLabel[3][1].setText(writeDataMissTime + "");
		if (writeDataMissTime + writeDataHitTime > 0) {
			double missRate = ((double) writeDataMissTime / (double) (writeDataMissTime + writeDataHitTime)) * 100;
			resultDataLabel[3][2].setText(String.format("%.2f", missRate) + "%");
		}

		if (inst.opt == 0) {
			accessTypeDataLabel.setText("读指令");
		} else if (inst.opt == 1) {
			accessTypeDataLabel.setText("读数据");
		} else if (inst.opt == 2) {
			accessTypeDataLabel.setText("写数据");
		} else {
			accessTypeDataLabel.setText("非法指令");
		}
		addressDataLabel.setText(inst.addr);
		blockNumberDataLabel.setText(inst.blockAddr + "");
		tagDataLabel.setText(inst.tag + "");
		indexDataLabel.setText(inst.index + "");
		inblockAddressDataLabel.setText(inst.inblockAddr + "");

		if (isHit) {
			hitDataLabel.setText("命中");
		} else {
			hitDataLabel.setText("未命中");
		}
	}

	/*
	 * 模拟执行到底
	 */
	private void simExecAll() {
		while (ip < isize) {
			simExecStep(false);
		}
	}

	/*
		辅助函数
	*/
	private int pow(int x, int p) {
		return (int) Math.pow(x, p);
	}

	private int log2(int x) {
		return (int) (Math.log(x) / Math.log(2));
	}

	private int random(int x, int y) {
		return (int) Math.random() * (y - x) + x;
	}

	/*
		绘制界面
	*/
	private void unifiedCacheEnabled(boolean enabled) {
		unifiedCacheButton.setSelected(enabled);
		csLabel.setEnabled(enabled);
		csBox.setEnabled(enabled);
	}

	private void separateCacheEnabled(boolean enabled) {
		separateCacheButton.setSelected(enabled);
		icsLabel.setEnabled(enabled);
		dcsLabel.setEnabled(enabled);
		icsBox.setEnabled(enabled);
		dcsBox.setEnabled(enabled);
	}

	private void draw() {
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

		labelTop = new JLabel("Cache Simulator by JCGuo");
		labelTop.setAlignmentX(CENTER_ALIGNMENT);
		JLabel promptLabel = new JLabel("  (Notice! 如果修改了左侧Cache设置请重新加载流文件再运行！)");
		promptLabel.setForeground(Color.red);
		panelTop.add(labelTop);
		panelTop.add(promptLabel);

		labelLeft = new JLabel("Cache 参数设置");
		labelLeft.setPreferredSize(new Dimension(300, 40));

		csLabel = new JLabel("总大小");
		csLabel.setPreferredSize(new Dimension(80, 30));
		csBox = new JComboBox<String>(cachesize);
		csBox.setPreferredSize(new Dimension(90, 30));
		csBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				csIndex = csBox.getSelectedIndex();
			}
		});
		//cache 种类
		unifiedCacheButton = new JRadioButton("统一Cache:", true);
		unifiedCacheButton.setPreferredSize(new Dimension(100, 30));
		unifiedCacheButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				separateCacheEnabled(false);
				unifiedCacheEnabled(true);
				cacheType = 0;
			}
		});

		separateCacheButton = new JRadioButton("分离Cache:");
		separateCacheButton.setPreferredSize(new Dimension(100, 30));
		separateCacheButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				separateCacheEnabled(true);
				unifiedCacheEnabled(false);
				cacheType = 1;
			}
		});

		icsLabel = new JLabel("指令Cache");
		icsLabel.setPreferredSize(new Dimension(80, 30));

		dcsLabel = new JLabel("数据Cache");
		dcsLabel.setPreferredSize(new Dimension(80, 30));

		JLabel emptyLabel = new JLabel("");
		emptyLabel.setPreferredSize(new Dimension(100, 30));

		icsBox = new JComboBox<String>(scachesize);
		icsBox.setPreferredSize(new Dimension(90, 30));
		icsBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				icsIndex = icsBox.getSelectedIndex();
			}
		});

		dcsBox = new JComboBox<String>(scachesize);
		dcsBox.setPreferredSize(new Dimension(90, 30));
		dcsBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				dcsIndex = dcsBox.getSelectedIndex();
			}
		});

		separateCacheEnabled(false);
		unifiedCacheEnabled(true);

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

		panelLeft.add(unifiedCacheButton);
		panelLeft.add(csLabel);
		panelLeft.add(csBox);

		panelLeft.add(separateCacheButton);
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
		panelRight.add(rightLabel);

		resultTagLabel = new JLabel[4][3];
		resultDataLabel = new JLabel[4][3];

		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 3; j++) {
				resultTagLabel[i][j] = new JLabel(resultTags[i][j]);
				resultTagLabel[i][j].setPreferredSize(new Dimension(70, 40));

				if (j != 2) {
					resultDataLabel[i][j] = new JLabel("0");
				} else {
					resultDataLabel[i][j] = new JLabel("0.00%");
				}

				resultDataLabel[i][j].setPreferredSize(new Dimension(83, 40));

				panelRight.add(resultTagLabel[i][j]);
				panelRight.add(resultDataLabel[i][j]);
			}
			if (i == 0) {
				JLabel label = new JLabel("其中:");
				label.setPreferredSize(new Dimension(500, 40));
				panelRight.add(label);
			}
		}

		/*
		stepLabel1 = new JLabel();
		stepLabel1.setVisible(false);
		stepLabel1.setPreferredSize(new Dimension(500, 40));
		stepLabel2 = new JLabel();
		stepLabel2.setVisible(false);
		stepLabel2.setPreferredSize(new Dimension(500, 40));
		
		
		
		
		panelRight.add(stepLabel1);
		panelRight.add(stepLabel2);
		*/

		accessTypeTagLabel = new JLabel("访问类型:");
		addressTagLabel = new JLabel("地址:");
		blockNumberTagLabel = new JLabel("块号:");
		tagTagLabel = new JLabel("标记Tag:");
		indexTagLabel = new JLabel("组索引:");
		inblockAddressTagLabel = new JLabel("块内地址:");
		hitTagLabel = new JLabel("命中情况:");

		accessTypeDataLabel = new JLabel("--");
		addressDataLabel = new JLabel("--");
		blockNumberDataLabel = new JLabel("--");
		tagDataLabel = new JLabel("--");
		indexDataLabel = new JLabel("--");
		inblockAddressDataLabel = new JLabel("--");
		hitDataLabel = new JLabel("--");

		accessTypeTagLabel.setPreferredSize(new Dimension(80, 40));
		accessTypeDataLabel.setPreferredSize(new Dimension(80, 40));
		addressTagLabel.setPreferredSize(new Dimension(80, 40));
		addressDataLabel.setPreferredSize(new Dimension(200, 40));
		panelRight.add(accessTypeTagLabel);
		panelRight.add(accessTypeDataLabel);
		panelRight.add(addressTagLabel);
		panelRight.add(addressDataLabel);

		blockNumberTagLabel.setPreferredSize(new Dimension(80, 40));
		blockNumberDataLabel.setPreferredSize(new Dimension(200, 40));
		hitTagLabel.setPreferredSize(new Dimension(80, 40));
		hitDataLabel.setPreferredSize(new Dimension(80, 40));
		panelRight.add(blockNumberTagLabel);
		panelRight.add(blockNumberDataLabel);
		panelRight.add(hitTagLabel);
		panelRight.add(hitDataLabel);

		tagTagLabel.setPreferredSize(new Dimension(60, 40));
		tagDataLabel.setPreferredSize(new Dimension(70, 40));
		indexTagLabel.setPreferredSize(new Dimension(60, 40));
		indexDataLabel.setPreferredSize(new Dimension(70, 40));
		inblockAddressTagLabel.setPreferredSize(new Dimension(60, 40));
		inblockAddressDataLabel.setPreferredSize(new Dimension(100, 40));
		panelRight.add(tagTagLabel);
		panelRight.add(tagDataLabel);
		panelRight.add(indexTagLabel);
		panelRight.add(indexDataLabel);
		panelRight.add(inblockAddressTagLabel);
		panelRight.add(inblockAddressDataLabel);

		//*****************************底部面板绘制*****************************************//

		bottomLabel = new JLabel("执行控制");
		bottomLabel.setPreferredSize(new Dimension(800, 30));
		execStepBtn = new JButton("步进");
		execStepBtn.setLocation(100, 30);
		execStepBtn.addActionListener(this);
		execAllBtn = new JButton("执行到底");
		execAllBtn.setLocation(300, 30);
		execAllBtn.addActionListener(this);

		panelBottom.add(bottomLabel);
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

	public static void main(String[] args) {
		new MyCacheSim();
	}
}
