package model;

import javax.swing.tree.DefaultMutableTreeNode;

public class OutlineNode extends DefaultMutableTreeNode {

  private String title;
  private String content;

  //Node im Tree
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

  @Override
  public void setUserObject(Object userObject) {
    super.setUserObject(userObject);
    this.title = userObject != null ? userObject.toString() : "";
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