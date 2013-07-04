/*
 * @author     ucchy, tsuttsu305
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2013
 */
package jp.ucchy;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.FilerException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * MD5チェッカー
 * 
 * @author ucchy
 */
public class MD5Checker {

    private JFrame frame;
    private JTextArea area;
    
    /**
     * メインメソッド
     * @param args 実行引数
     */
    public static void main(String[] args) {

        // 引数に指定されたものは、解析対象とする
        ArrayList<File> files = new ArrayList<File>();
        for ( String a : args ) {
            File f = new File(a);
            if ( f.exists() && f.isFile() ) {
                files.add(f);
            }
        }
        
        // UIを開く
        MD5Checker checker = new MD5Checker();
        checker.showGUI(files);
    }

    /**
     * GUIを開きます
     * @param files 引数で指定されたファイル
     */
    private void showGUI(ArrayList<File> files) {

        frame = new JFrame("MD5 Checker");
        frame.setSize(700, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // テキストエリアを作ってフレームに追加
        area = new JTextArea();
        area.setEditable(false);

        JScrollPane scrollpane = new JScrollPane(area,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        
        frame.add(scrollpane);

        // ドラッグアンドドロップをサポートする
        DropTargetListener dtl = new DropTargetAdapter() {
            
            @Override
            public void dragOver(DropTargetDragEvent dtde) {
                if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    dtde.acceptDrag(DnDConstants.ACTION_COPY);
                    return;
                }
                dtde.rejectDrag();
            }

            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    
                    if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        
                        dtde.acceptDrop(DnDConstants.ACTION_COPY);
                        Transferable t = dtde.getTransferable();
                        Object obj = t.getTransferData(DataFlavor.javaFileListFlavor);
                        
                        @SuppressWarnings("unchecked")
                        List<File> list = (List<File>)obj;
                        
                        for ( File f : list ) {
                            String message = String.format("%s → %s\r\n", 
                                    f.getAbsolutePath(), getFileMd5(f) );
                            area.append(message);
                        }
                        
                        dtde.dropComplete(true);
                        return;
                    }
                } catch (UnsupportedFlavorException e) {
                    e.printStackTrace();
                    showErrorDialog(e.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                    showErrorDialog(e.toString());
                }
                dtde.rejectDrop();
            }
        };
        new DropTarget(area, DnDConstants.ACTION_COPY, dtl, true);

        // 引数で指定されたファイルを解析して、あらかじめ表示しておく
        try {
            for ( File f : files ) {
                String message = String.format("%s → %s\r\n", 
                        f.getAbsolutePath(), getFileMd5(f) );
                area.append(message);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            showErrorDialog(e.toString());
        } catch (FilerException e) {
            e.printStackTrace();
            showErrorDialog(e.toString());
        }
        
        // 表示
        frame.setVisible(true);
    }
    
    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(frame, message, "error", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * ファイルのMD5を取得する
     * 
     * @param file MD5を取得するFile
     * @return MD5
     * @throws FileNotFoundException ファイルが存在しない
     * @throws FilerException 指定したFileはDirectoryである
     */
    public static String getFileMd5(File file) throws FileNotFoundException,
            FilerException {

        // ファイルのみ対応
        if (!file.exists())
            throw new FileNotFoundException(
                    file.getAbsolutePath() + " was not found!");
        if (file.isDirectory())
            throw new FilerException(file.getAbsolutePath() + " is Directory!");

        // MD5計算
        DigestInputStream din = null;
        byte[] md5 = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            din = new DigestInputStream(new BufferedInputStream(
                    new FileInputStream(file)), md);

            // バッファに貯まるまで待機
            while (din.read() != -1) {
            }

            // MD5をbyte配列に格納
            md5 = md.digest();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (din != null) {
                try {
                    din.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return toHexadecimal(md5);
    }
    
    private static String toHexadecimal(byte[] bytes) {
        
        StringBuilder str = new StringBuilder();
        for ( byte b : bytes ) {
            str.append(String.format("%02x", b));
        }
        return str.toString();
    }
}
