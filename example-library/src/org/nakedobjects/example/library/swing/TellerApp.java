/*
	Naked Objects - a framework that exposes behaviourally complete
	business objects directly to the user.
	Copyright (C) 2000 - 2003  Naked Objects Group Ltd

	This program is free software; you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation; either version 2 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program; if not, write to the Free Software
	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

	The authors can be contacted via www.nakedobjects.org (the
	registered address of Naked Objects Group is Kingsway House, 123 Goldworth
	Road, Woking GU21 1NR, UK).
*/

package org.nakedobjects.example.library.swing;

import java.awt.HeadlessException;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


public class TellerApp extends JFrame {
	private JTextField bookCode;
	private JTextArea bookInfo;
	private JButton checkoutBookButton;
	private JTextField memberCode;
	private JTextArea memberInfo;
	private DefaultListModel bookList;
	protected CheckoutSession session;
	
	
	public TellerApp() throws HeadlessException {
		super("Library System");

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				quit();
			}
		});
		
		JPanel contents = new JPanel();
		setContentPane(contents);
		
		MenuBar menuBar = new MenuBar();
		setMenuBar(menuBar);
		
		Menu main = new Menu("App");
		MenuItem exit = new MenuItem("Exit");
		exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { quit(); }
		});
		main.add(exit);
		menuBar.add(main);
		
		contents.add(new JLabel("Member code"));
		contents.add(memberCode = new JTextField(10));

		JButton findMemberButton;
		contents.add(findMemberButton = new JButton("Find"));
		findMemberButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				session = new CheckoutSession(memberCode.getText(), TellerApp.this);
			}
		});

		memberInfo = new JTextArea(5, 40);
		contents.add(memberInfo);
		
		
		contents.add(new JLabel("Book code"));
		contents.add(bookCode = new JTextField(10));

		JButton findBookButton;
		contents.add(findBookButton = new JButton("Find"));
		findBookButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				session.actionFindBook(bookCode.getText());
			}
		});
		
		bookInfo = new JTextArea(5, 40);
		contents.add(bookInfo);
		
		checkoutBookButton = new JButton("Take out book");
		checkoutBookButton.setEnabled(false);
		contents.add(checkoutBookButton);
		checkoutBookButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				session.actionTakeOutBook(bookCode.getText());
			}
		});
		
		bookList = new DefaultListModel();
		JList books = new JList(bookList);
		contents.add(new JScrollPane(books));
	}


	private void quit() {
		dispose();
		System.exit(0);
	}

	public static void main(String[] args) {
		TellerApp ta = new TellerApp();
		ta.setSize(500, 400);
		ta.show();
	}
	
	public void addBook(String bookDetail) {
		bookList.add(0, bookDetail);
	}


	public void showMember(String memberDetail) {
		memberInfo.setText(memberDetail);
	}


	public void showBook(String bookDetail) {
		checkoutBookButton.setEnabled(!bookDetail.equals(""));
		bookInfo.setText(bookDetail);
	}
}
