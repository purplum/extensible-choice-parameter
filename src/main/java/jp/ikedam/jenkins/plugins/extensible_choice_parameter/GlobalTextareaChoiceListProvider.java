package jp.ikedam.jenkins.plugins.extensible_choice_parameter;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.util.ListBoxModel;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import net.sf.json.JSONObject;

/**
 * A choice provider whose choices are defined
 * in the System Configuration page, and can be refereed from all jobs.
 */
public class GlobalTextareaChoiceListProvider extends ChoiceListProvider implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    /**
     * The internal class to work with views.
     * Also manage the global configuration.
     * 
     * The following files are used (put in main/resource directory in the source tree).
     * <dl>
     *     <dt>config.jelly</dt>
     *         <dd>
     *             Shown as a part of a job configuration page when this provider is selected.
     *             Provides additional configuration fields of a Extensible Choice.
     *         </dd>
     *     <dt>global.jelly</dt>
     *         <dd>
     *              Shown as a part of the System Configuration page.
     *              Call config.jelly of GlobalTextareaChoiceListEntry,
     *              for each set of choices.
     *         </dd>
     *     </dt>
     * </dl>
     */
    @Extension
    public static class DescriptorImpl extends Descriptor<ChoiceListProvider>
    {
        /**
         * Restore from the global configuration
         */
        public DescriptorImpl()
        {
            load();
        }
        
        private List<GlobalTextareaChoiceListEntry> choiceListEntryList;
        
        /**
         * The list of available sets of choices.
         * 
         * @return the list of GlobalTextareaChoiceListEntry
         */
        public List<GlobalTextareaChoiceListEntry> getChoiceListEntryList()
        {
            return choiceListEntryList;
        }
        
        /**
         * Set a list of available sets of choices.
         * 
         * @param choiceListEntryList a list of GlobalTextareaChoiceListEntry
         */
        @SuppressWarnings("unchecked") // for the cast to List<GlobalTextareaChoiceListEntry>
        public void setChoiceListEntryList(List<GlobalTextareaChoiceListEntry> choiceListEntryList){
            // Invalid values may be submitted.
            // (Jenkins framework seems not to forbid the submission,
            // even if form validations alert errors...)
            // retrieve only valid (correctly configured) entries
            this.choiceListEntryList = 
                (List<GlobalTextareaChoiceListEntry>)CollectionUtils.select(
                        choiceListEntryList,
                        new Predicate()
                        {
                            @Override
                            public boolean evaluate(Object entry)
                            {
                                return ((GlobalTextareaChoiceListEntry)entry).isValid();
                            }
                        }
                );
        }
        
        /**
         * Returns a list of the names of the available choice set.
         * 
         * Used in dropdown field of a job configuration page.
         * 
         * @return a list of the names of the available choice set
         */
        public ListBoxModel doFillNameItems()
        {
            ListBoxModel m = new ListBoxModel();
            if(getChoiceListEntryList() != null)
            {
                for(GlobalTextareaChoiceListEntry e: getChoiceListEntryList())
                {
                    m.add(e.getName());
                }
            }
            return m;
        }
        
        /**
         * Retrieve the set of choices entry by the name.
         * @param name
         * @return the set of choices.
         */
        public GlobalTextareaChoiceListEntry getChoiceListEntry(String name)
        {
            for(GlobalTextareaChoiceListEntry e: getChoiceListEntryList())
            {
                if(e.getName().equals(name))
                {
                    return e;
                }
            }
            return null;
         }
        
        /**
         * Retrieve the set of choices entry by the name.
         * @param name
         * @return the list of choices.
         */
        public List<String> getChoiceList(String name)
        {
           GlobalTextareaChoiceListEntry e = getChoiceListEntry(name);
           return (e != null)?e.getChoiceList():new ArrayList<String>();
        }
        
        /**
         * the display name shown in the dropdown to select a choice provider.
         * 
         * @return display name
         * @see hudson.model.Descriptor#getDisplayName()
         */
        @Override
        public String getDisplayName()
        {
            return Messages._GlobalTextareaChoiceListProvider_DisplayName().toString();
        }
        
        /**
         * Store the parameters specified in the System Configuration page.
         * 
         * @param req
         * @param formData
         * @return whether succeeded to store. 
         * @throws FormException
         * @see hudson.model.Descriptor#configure(org.kohsuke.stapler.StaplerRequest, net.sf.json.JSONObject)
         */
        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException
        {
            setChoiceListEntryList(req.bindJSONToList(GlobalTextareaChoiceListEntry.class, formData.get("choiceListEntryList")));
            
            save();
            
            return super.configure(req,formData);
        }
    }
    
    private String name = null;
    
    /**
     * Returns the name of the set of choice, specified by a user.
     * 
     * @return the name of the set of choice
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * Returns the choices available as a parameter value. 
     * 
     * @return choices
     * @see jp.ikedam.jenkins.plugins.extensible_choice_parameter.ChoiceListProvider#getChoiceList()
     */
    @Override
    public List<String> getChoiceList()
    {
        return ((DescriptorImpl)getDescriptor()).getChoiceList(getName());
    }
    
    /**
     * Constructor instantiating with parameters in the configuration page.
     * 
     * When instantiating from the saved configuration,
     * the object is directly serialized with XStream,
     * and no constructor is used.
     * 
     * @param name the name of the set of choices.
     */
    @DataBoundConstructor
    public GlobalTextareaChoiceListProvider(String name)
    {
        // No validation is performed, for the name is selected from the dropdown.
        this.name = name;
    }
}