package model;

import javax.swing.tree.DefaultMutableTreeNode;

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
}