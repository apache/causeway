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
package org.nakedobjects.viewer.lightweight;

import java.awt.event.KeyEvent;
import java.util.Vector;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.reflect.Field;


class PopupMenu extends AbstractView implements KeyboardAccessible {
    private Color backgroundColor;
    private View forView;
    private Item[] items = new Item[0];
    private int optionIdentified;
	private Location mouseAt;

    public Padding getPadding() {
        Padding in = super.getPadding();
        in.top += VPADDING;
        in.bottom += VPADDING;
        in.left += HPADDING + 5;
        in.right += HPADDING + 5;

        return in;
    }

    public Size getRequiredSize() {
        Padding in = getPadding();

        int height = 0;
        int width = 0;

        // TODO
        for (int i = 0; i < items.length; i++) {
            int itemWidth = items[i].isBlank ? 0 : style().stringWidth(items[i].name);
            width = Math.max(width, itemWidth);
            height += style().getHeight() + VPADDING;
        }

        height += (in.top + in.bottom);
        width += (in.left + in.right) + HPADDING * 2;

        return new Size(width, height);
    }

    protected Style.Text style() {
		return Style.MENU;
	}

	public boolean canChangeValue() {
        return false;
    }

    public boolean canFocus() {
        return false;
    }

    /**
     * Draws the popup menu
     * @see java.awt.Component#paint(Canvas)
     */
    public void draw(Canvas canvas) {
        super.draw(canvas);

        int width = getSize().width;
        int height = getSize().height;
        canvas.draw3DRectangle(0, 0, width - 1, height - 1, true);

        int baseLine = style().getAscent() + VPADDING;
        int left = getPadding().getLeft();

//		canvas.drawText(title, left, baseLine, Style.NORMAL_MENU, style);

        for (int i = 0; i < items.length; i++) {
            if (items[i].isBlank) {
                int y = baseLine - (style().getAscent() / 2);
                canvas.drawLine(1, y, width - 2, y, backgroundColor.brighter());
                canvas.drawLine(1, y - 1, width - 2, y - 1, backgroundColor.darker());
            } else {
                Color color;

                if (items[i].isDisabled || items[i].action == null) {
                    color = disabledColor();
                } else if (getOption() == i) {
                    canvas.drawFullRectangle(2, baseLine - style().getAscent(), width - 4,
                        style().getHeight() + 2, backgroundColor.darker());
                    color = reversedColor();
                } else {
                    color = normalColor();
                }

                canvas.drawText(items[i].name, left, baseLine, color, style());
            }

            baseLine += style().getHeight() + VPADDING;
        }
    }

    protected Color normalColor() {
		return Style.NORMAL_MENU;
	}

	protected Color reversedColor() {
		return Style.REVERSE_MENU;
	}

	protected Color disabledColor() {
		return Style.DISABLED_MENU;
	}

	public void editComplete() {
    }

    public void firstClick(Click click) {
    	mouseMoved(click.getLocation());
        invoke();
    }

	public void focusLost() {
    }

    public void focusRecieved() {
    }

    public boolean hasFocus() {
        return false;
    }

    public void init(View over, Location mouseAt, boolean forView, boolean includeExploration, boolean includeDebug) {
        this.forView = over;
        
		MenuOptionSet optionSet = new MenuOptionSet(forView);
		over.menuOptions(optionSet);

		optionIdentified = 0;
        backgroundColor = optionSet.getColor();

        Vector options = optionSet.getMenuOptions(includeExploration, includeDebug);
        int len = options.size();
        if(len == 0) {
        	items = new Item[] {new Item()};
        } else {
	        items = new Item[len];
	
	        for (int i = 0; i < len; i++) {
	            items[i] = new Item((UserAction) options.elementAt(i), getWorkspace(), over,
	                    getLocation());
	        }
        }
        
        Size size = getRequiredSize();
        setSize(size);
        this.mouseAt = mouseAt;
		Location location = new Location(mouseAt);
		location.translate(-14, -10);
        setLocation(location);
        
    }

    public void invoke() {
    	int option = getOption();

    	if (option >= 0) {
    		Workspace workspace = getWorkspace();
    		Location location = new Location(mouseAt);
    		location.translate(30, 12);
    		Item item = items[option];

    		workspace.setStatus("");
    		workspace.clearOverlayView();
    		if (!item.isBlank && item.action != null && item.action.disabled(workspace, forView, location).isAllowed()) {
    			item.action.execute(workspace, forView, location);
    		}
    	}
    }

    public void keyPressed(int keyCode, int modifiers) {
        if (keyCode == KeyEvent.VK_ESCAPE) {
			getWorkspace().clearOverlayView();

        } else if (keyCode == KeyEvent.VK_ENTER) {
			invoke();

		} else if (keyCode == KeyEvent.VK_UP) {
			for(int i = optionIdentified - 1; i >= 0; i--) {
				if(items[i].isBlank) {
					continue;
				}
				if(items[i].isDisabled) {
					continue;
				}
				setOption(i);
				break;
			}

		} else if (keyCode == KeyEvent.VK_DOWN) {
			for(int i = optionIdentified + 1; i < items.length; i++) {
				if(items[i].isBlank) {
					continue;
				}
				if(items[i].isDisabled) {
					continue;
				}
				setOption(i);
				break;
			}
		}
    }

    public void keyReleased(int keyCode, int modifiers) {
    }

    public void keyTyped(char keyCode) {
    }

    public View makeView(Naked object, Field field) throws CloneNotSupportedException {
        throw new RuntimeException();
    }

    public void menuOptions(MenuOptionSet menuOptions) {
    }

    public void mouseMoved(Location at) {
    	int option = at.getY() / (style().getHeight() + VPADDING);
		option = Math.max(option, 0);
		option = Math.min(option, items.length - 1);
		if(option >= 0) {
			setOption(option);
		}
    }

    protected Color backgroundColor() {
        return backgroundColor;
    }

    protected boolean transparentBackground() {
        return false;
    }

    public void setOption(int option) {
    	if (option != optionIdentified) {
            optionIdentified = option;
			if (items[optionIdentified].isBlank) {
				getWorkspace().setStatus("");
			} else {
				getWorkspace().setStatus(items[optionIdentified].reason);
			}
            redraw();
        }
    }

    public int getOption() {
        return optionIdentified;
    }

	public int getOptionCount() {
		return items.length;
	}

    private static class Item {
		UserAction action;
        String name;
        String reason;
        boolean isBlank;
        boolean isDisabled;

        Item() {
        	name = "no option";
        }
        
        Item(UserAction action, Workspace workspace, View view, Location at) {
            if (action == null) {
                isBlank = true;
            } else {
                isBlank = false;
                this.action = action;
                name = action.getName(workspace, view, at);
                isDisabled = action.disabled(workspace, view, at).isVetoed();
                reason = action.disabled(workspace, view, at).getReason();
            }
        }
    }
    
    public String toString() {
		return "PopupMenu [location=" + getLocation() + ",item=" + optionIdentified + ",itemCount=" + (items == null ? 0 : items.length) + "]";
	}
}
