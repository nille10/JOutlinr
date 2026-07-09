package model;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Repräsentiert einen Knoten im Outline-Baum.
 * Jeder Knoten hat einen Titel und optional einen langen Textinhalt.
 */
public class OutlineNode extends DefaultMutableTreeNode {

  private String title;
  private String content;

  public OutlineNode(String title) {
    super(title);
    this.title = title;
    this.content = "";
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
    setUserObject(title);
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  @Override
  public String toString() {
    return title;
  }

  //für Kopie mit Kindknoten
  public OutlineNode deepCopy() {

    OutlineNode copy = new OutlineNode(title);
    copy.setContent(content);

    for (int i = 0; i < getChildCount(); i++) {
      OutlineNode child = (OutlineNode) getChildAt(i);
      copy.add(child.deepCopy());
    }

    return copy;
  }
}