import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EtchedBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import javafx.scene.control.Separator;

public class CCacheSim extends JFrame implements ActionListener {

	private JPanel panelTop, panelLeft, panelRight, panelBottom;
	private JButton execStepBtn, execAllBtn, fileBotton;
	private JComboBox<String> csBox, icsBox, dcsBox, bsBox, wayBox, replaceBox, prefetchBox, writeBox, allocBox;
	private JFileChooser fileChoose;

	private JLabel labelTop, labelLeft, rightLabel, bottomLabel, fileLabel, fileAddrBtn, stepLabel1, stepLabel2,
			csLabel, icsLabel, dcsLabel, emptyLabel, bsLabel, wayLabel, replaceLabel, prefetchLabel, writeLabel,
			allocLabel;
	private JLabel results[];
	private JRadioButton unifiedcacheButton, separatecacheButton;

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
	private String rightLable[] = { "访问总次数：", "读指令次数：", "读数据次数：", "写数据次数：" };

	//打开文件
	private File file;

	//分别表示左侧几个下拉框所选择的第几项，索引从 0 开始
	private int csIndex, icsIndex, dcsIndex, bsIndex, wayIndex, replaceIndex, prefetchIndex, writeIndex, allocIndex;

	//其它变量定义
	//...
	private int CacheMod = 0;

	/*
	 * 构造函数，绘制模拟器面板
	 */
	public CCacheSim() {
		super("Cache Simulator");
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
			simExecStep();
		}
		if (e.getSource() == fileBotton) {
			int fileOver = fileChoose.showOpenDialog(null);
			if (fileOver == 0) {
				String path = fileChoose.getSelectedFile().getAbsolutePath();
				fileAddrBtn.setText(path);
				file = new File(path);
				readFile();
				initCache();
			}
		}
	}

	/*
	 * 初始化 Cache 模拟器
	 */
	public void initCache() {

	}

	/*
	 * 将指令和数据流从文件中读入
	 */
	public void readFile() {

	}

	/*
	 * 模拟单步执行
	 */
	public void simExecStep() {

	}

	/*
	 * 模拟执行到底
	 */
	public void simExecAll() {

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

		} else {
			separatecacheButton.setSelected(true);
			icsBox.setEnabled(true);
			icsLabel.setEnabled(true);
			dcsLabel.setEnabled(true);
			dcsBox.setEnabled(true);
			unifiedcacheButton.setSelected(false);
			csBox.setEnabled(false);
			csLabel.setEnabled(false);
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
		results = new JLabel[4];
		for (int i = 0; i < 4; i++) {
			results[i] = new JLabel("");
			results[i].setPreferredSize(new Dimension(500, 40));
		}

		stepLabel1 = new JLabel();
		stepLabel1.setVisible(false);
		stepLabel1.setPreferredSize(new Dimension(500, 40));
		stepLabel2 = new JLabel();
		stepLabel2.setVisible(false);
		stepLabel2.setPreferredSize(new Dimension(500, 40));

		panelRight.add(rightLabel);
		for (int i = 0; i < 4; i++) {
			panelRight.add(results[i]);
		}

		panelRight.add(stepLabel1);
		panelRight.add(stepLabel2);

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

}
