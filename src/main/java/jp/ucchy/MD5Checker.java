/*
 * @author     ucchy, tsuttsu305
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2013
 */
package jp.ucchy;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
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
                            String message = String.format("%s：%s\r\n",
                                    getFileMd5(f), f.getAbsolutePath() );
                            area.append(message);
                        }

                        dtde.dropComplete(true);
                        return;
                    }
                } catch (Exception e) {
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
                String message = String.format("%s：%s\r\n",
                        getFileMd5(f), f.getAbsolutePath() );
                area.append(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorDialog(e.toString());
        }

        // 表示
        frame.setVisible(true);
    }

    /**
     * エラーダイアログを表示する。
     * @param message エラーメッセージ
     */
    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(
                frame, message, "error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * ファイルのMD5を取得する
     *
     * @param file MD5を取得するFile
     * @return MD5
     * @throws FileNotFoundException ファイルが存在しない
     * @throws FilerException 指定したFileはDirectoryである
     */
    public String getFileMd5(File file)
            throws NoSuchAlgorithmException, IOException,
                    FileNotFoundException, FilerException {

        // ファイルのみ対応
        if (!file.exists())
            throw new FileNotFoundException(
                    file.getAbsolutePath() + " was not found!");
        if (file.isDirectory())
            throw new FilerException(file.getAbsolutePath() + " is folder!");

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
            throw e;
        } catch (IOException e) {
            throw e;
        } finally {
            if (din != null) {
                try {
                    din.close();
                } catch (IOException e) {
                    // do nothing.
                }
            }
        }

        return toHexadecimal(md5);
    }

    /**
     * 指定されたbyte配列を、16進数表示に変換して返します。
     * @param bytes byte配列
     * @return 16進数表示の文字列
     */
    private String toHexadecimal(byte[] bytes) {

        StringBuilder str = new StringBuilder();
        for ( byte b : bytes ) {
            str.append(String.format("%02x", b));
        }
        return str.toString();
    }
}
