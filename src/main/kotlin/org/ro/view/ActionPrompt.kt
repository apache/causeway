package org.ro.view

import org.ro.core.Utils
import org.ro.to.Action
import org.ro.to.Link
import org.ro.to.Parameter
import pl.treksoft.kvision.core.StringPair
import pl.treksoft.kvision.form.select.Select

class ActionPrompt(val action: Action) : Command {

    fun open() {
        console.log("[ActionPrompt.open] ${action}")
        val formItems = mutableListOf<FormItem>()
        formItems.add(FormItem("Script", "Select", ""))
        formItems.add(FormItem("Parameters", "TextArea", ""))
        RoDialog(label = label(), items = formItems, command = this).show()
    }

    override fun execute() {
        console.log("[ActionPrompt.execute]")
        //FIXME script name is mandatory and needs to be passed
        val l = action.getInvokeLink()
        l!!.invoke()
    }

    private fun label(): String {
        val label = Utils().deCamel(action.id);
        return "Execute: $label"
    }

    protected fun populateForm() {
        val params: Collection<Parameter> = action.parameters.values
        for (p: Parameter in params) {
            //          val fi: FormItem = UIUtil.buildFormItem(p.name);
            var input: UIComponent;
            val elements: List<StringPair> = mutableListOf<StringPair>() //p.choices
            val cb = Select(elements);
            // cb.dataProvider = VectorList(p.getChoiceListKeys());
            //cb. = p.findIndexOfDefaultChoice();
            if (p.defaultChoice != null) {
                //  fi.required = true;
            }
            //  input = cb;
        } /*else {
            input = Text();
        }
        fi.addElement(input);
        form.addElement(fi); */
    }


    fun okHandler() {
        val l: Link? = action.getInvokeLink();
        //iterate over FormItems (0,1, but not 2 (buttons)
        var fi: FormItem;
        var key: String;
        var input: UIComponent;
        var value: String;
        /*
        for (i: Int; i < form.numElements; i++) {
            fi = form.getElementAt(i) as FormItem;
            key = fi.label;
            input = fi.getElementAt(0) as UIComponent;
            if (input is TextInput) {
                val ti: TextInput = input as TextInput;
                val = ti.text;
                l.setArgument(key, value)
            } else if (input is Select) {
                val ddl: Select = input as Select;
                val selection: String = ddl.selectedLabel;
                val p: Parameter = this.action.findParameterByName(key.toLowerCase());
                val href: String = p.getHrefByTitle(selection);
                l.setArgument(key, href)
            }
        }
            */
        l!!.invoke();
        //    close();
    }

}