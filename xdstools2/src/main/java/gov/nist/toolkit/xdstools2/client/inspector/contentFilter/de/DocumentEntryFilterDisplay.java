package gov.nist.toolkit.xdstools2.client.inspector.contentFilter.de;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import gov.nist.toolkit.http.client.HtmlMarkup;
import gov.nist.toolkit.registrymetadata.client.Author;
import gov.nist.toolkit.registrymetadata.client.DocumentEntry;
import gov.nist.toolkit.results.client.Result;
import gov.nist.toolkit.xdstools2.client.inspector.CommonDisplay;
import gov.nist.toolkit.xdstools2.client.inspector.HyperlinkFactory;
import gov.nist.toolkit.xdstools2.client.inspector.contentFilter.FilterFeature;
import gov.nist.toolkit.xdstools2.client.inspector.contentFilter.IndexFieldValue;
import gov.nist.toolkit.xdstools2.client.inspector.contentFilter.de.component.AuthorFieldFilterSelector;
import gov.nist.toolkit.xdstools2.client.inspector.contentFilter.de.component.CreationTimeFieldFilterSelector;
import gov.nist.toolkit.xdstools2.client.inspector.contentFilter.de.component.DateRangeFieldFilter;
import gov.nist.toolkit.xdstools2.client.inspector.contentFilter.de.component.EntryTypeFieldFilterSelector;
import gov.nist.toolkit.xdstools2.client.inspector.contentFilter.de.component.IndexFieldFilterSelector;
import gov.nist.toolkit.xdstools2.client.inspector.contentFilter.de.component.NewSelectedFieldValue;
import gov.nist.toolkit.xdstools2.client.inspector.contentFilter.de.component.ServiceStartTimeFieldFilterSelector;
import gov.nist.toolkit.xdstools2.client.inspector.contentFilter.de.component.ServiceStopTimeFieldFilterSelector;
import gov.nist.toolkit.xdstools2.client.inspector.contentFilter.de.component.StatusFieldFilterSelector;
import gov.nist.toolkit.xdstools2.client.util.SimpleCallbackT;
import gov.nist.toolkit.xdstools2.client.widgets.PopupMessage;
import gov.nist.toolkit.xdstools2.client.widgets.queryFilter.CodeFilterBank;
import gov.nist.toolkit.xdstools2.client.widgets.queryFilter.StatusDisplay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import static gov.nist.toolkit.http.client.HtmlMarkup.red;

public class DocumentEntryFilterDisplay extends CommonDisplay implements FilterFeature<DocumentEntry> {
    Map<String, List<String>> codeSpecMap = new HashMap<String, List<String>>();

    // Main body
    boolean isFilterApplied = false;
    private FlowPanel featurePanel = new FlowPanel();
    private VerticalPanel previousPanel;
    // controls
    private TextBox pidTxt = new TextBox();
    private TextBox titleTxt = new TextBox();
    private TextBox commentsTxt = new TextBox();
    private TextBox languageCodeTxt = new TextBox();
    private TextBox legalAuthenticatorTxt = new TextBox();
    private ListBox sourcePatientInfoLBox = new ListBox();
    CodeFilterBank codeFilterBank;
    HTML statusBox = new HTML();
    VerticalPanel resultPanel = new VerticalPanel();
    StatusDisplay statusDisplay = new StatusDisplay() {
        @Override
        public VerticalPanel getResultPanel() {
            return resultPanel;
        }

        @Override
        public void setStatus(String message, boolean status) {
            statusBox.setHTML(HtmlMarkup.bold(red(message, status)));
        }
    };

    private LinkedList<IndexFieldFilterSelector<DocumentEntryIndexField,DocumentEntry>> filterSelectors;
//    private Map<DocumentEntryIndexField, Map<IndexFieldValue, List<DocumentEntry>>> fieldIndexMap;
    private List<DocumentEntry> initialDeList;
    private static boolean isActive = false;
    SimpleCallbackT<NewSelectedFieldValue> valueChangeCallback;

    // enum of fields

    // index field:
    //  type:Enum
    // search widgets:
    // a doubly linked list of
    //  enum
    //  widget, call the constructor with the enum type and a callback to doIndex(indexField:enum,selectedFieldValues:list<String>).
    //  result of doIndex:list<de>. This is an aggregate list.
    // refresh widget result count:
    // for each fieldValue code result, call widget.updateCount(fieldValue:String,count:Int)

    // fill the search screen in the order of the linked list

   // skb TODO: map here
    // initial state just shows independent count for each field-value.
    // when a field value is selected, changed:
    // doIndex(searchField:Enum, list<de>):
    //      skip all previous fields and doIndex for the field that changed and if other field values are present after this one.
    //          Use the list<de> from the previous field if exists else use the main Mc.
    //      from list<de> this map is created: map[field:Enum, map[field-value:String,list<de>]
    //          example:                       DocEntryStatus, [stable=list,ondemand=list,both=list]
    //      Sort the map.keySet for Time fields
    // iM = initial map
    //
    // fM = filtered map
    // initially fM is a deep copy of iM
    // on search field S selected:
    // fmTemp: list<de> = new temporary storage
    //      get fM map by field
    //         for each selected-value
    //          get value-map by the field-value
    //              add to fMTemp(list<de>)
    //          count fMTemp, if > 0, fM = doIndex(fMTemp), lock the search field S so it cannot be changed until it is cleared.  When a search field is cleared, the iM map must be used!
    // locking the search field:
    //      field becomes read-only with a count and an X to clear search term.
    // map[field, map[code,list<de>]

    public DocumentEntryFilterDisplay() {
        filterSelectors = new LinkedList<>();

        valueChangeCallback =  new SimpleCallbackT<NewSelectedFieldValue>() {
            @Override
            public void run(NewSelectedFieldValue newSelectedValue) {
                // 1. reIndex
//                filterSelectors.listIterator()
                int idx = filterSelectors.indexOf(newSelectedValue.getFilterSelector());
                ListIterator<IndexFieldFilterSelector<DocumentEntryIndexField,DocumentEntry>> it = filterSelectors.listIterator(idx);
//                GWT.log("idx: " + idx);
                if (it!=null) {
                    List<DocumentEntry> list = null;
                    if (it.hasPrevious()) {
                        list = it.previous().getResult();
                        it.next(); // forward to the actual selector that raised this event
                    } else if (idx == 0) {
                        list = new ArrayList<>();
                        list.addAll(initialDeList);
                    }
                    if (list!=null) {
                        if (list.isEmpty()) {
                            while (it.hasNext()) {
                                IndexFieldFilterSelector<DocumentEntryIndexField, DocumentEntry> selector = it.next();
                                selector.clearResult();
                            }
                        } else {
                            List<DocumentEntry> filteredResult = new ArrayList<>();
                            filteredResult.addAll(list);
                            while (it.hasNext()) {
                                IndexFieldFilterSelector<DocumentEntryIndexField, DocumentEntry> selector = it.next();
                                Map<DocumentEntryIndexField, Map<IndexFieldValue, List<DocumentEntry>>> fieldMap = DocumentEntryIndex.indexMap(selector.getFieldType(), filteredResult);
//                                GWT.log("Selector: " + selector.getFieldType().name());
                                selector.clearResult();
//                                GWT.log("initial filteredResult size is: " + filteredResult.size());
//                                GWT.log("About to begin filtering...");
                                if (newSelectedValue.isClearSelection()) {
//                                    GWT.log("clearing field: initial fieldMap size is: " + fieldMap.size());
                                    selector.addResult(filteredResult);
                                } else {
//                                    GWT.log("field selected: initial fieldMap size is: " + fieldMap.size());
                                    if (!selector.isDeferredIndex() && fieldMap.isEmpty()) {
                                        filteredResult.clear();
                                    } else {
                                        if (selector instanceof DateRangeFieldFilter) {
                                            String fromDt = ((DateRangeFieldFilter)selector).getFromDt();
                                            String toDt = ((DateRangeFieldFilter)selector).getToDt();
                                            for (IndexFieldValue ifv : fieldMap.get(selector.getFieldType()).keySet()) {
                                                List<DocumentEntry> deList = fieldMap.get(selector.getFieldType()).get(ifv);
                                                if (deList!=null) {
                                                    selector.doUpdateCount(ifv, deList.size());
                                                    selector.addResult(DateRangeFieldFilter.filterByCreationTime(fromDt, toDt, deList));
                                                } else {
                                                    selector.doUpdateCount(ifv, 0);
                                                }
                                            }
                                            ((DateRangeFieldFilter)selector).setAvailableDates();
                                        } else if (selector.isDeferredIndex()) {
                                            if (selector instanceof AuthorFieldFilterSelector) {
                                                    AuthorFieldFilterSelector affs = (AuthorFieldFilterSelector) selector;
                                                    List<DocumentEntry> result = affs.filterByAuthorPerson(filteredResult);
                                                    selector.doUpdateCount(null, result.size());
                                                    selector.addResult(result);
                                            }
                                        } else {
                                            // Default to simple String value
                                            for (IndexFieldValue ifv : fieldMap.get(selector.getFieldType()).keySet()) {
                                                List<DocumentEntry> deList = fieldMap.get(selector.getFieldType()).get(ifv);
                                                if (deList!=null) {
                                                    selector.doUpdateCount(ifv, deList.size());
                                                    if (newSelectedValue.isInitialValue() || (selector.getSelectedValues()!=null && selector.getSelectedValues().contains(ifv)))
                                                        selector.addResult(deList);
                                                } else {
                                                    selector.doUpdateCount(ifv, 0);
                                                }
                                            }
                                        }
                                        filteredResult.clear();
                                        filteredResult.addAll(selector.getResult());
                                    }
                                }
//                                GWT.log("done. filtered size is: " + filteredResult.size());
                            }
                        }
                    }
                }
            }
        };

        final IndexFieldFilterSelector<DocumentEntryIndexField,DocumentEntry> statusFilterSelector = new StatusFieldFilterSelector("DocumentEntry Status", valueChangeCallback);
        final IndexFieldFilterSelector<DocumentEntryIndexField,DocumentEntry> entryTypeFilterSelector = new EntryTypeFieldFilterSelector("DocumentEntry Type", valueChangeCallback);
        final IndexFieldFilterSelector<DocumentEntryIndexField,DocumentEntry> creationTimeFilterSelector = new CreationTimeFieldFilterSelector("Creation Time", valueChangeCallback);
        final IndexFieldFilterSelector<DocumentEntryIndexField,DocumentEntry> serviceStartTimeFilterSelector = new ServiceStartTimeFieldFilterSelector("Service Start Time", valueChangeCallback);
        final IndexFieldFilterSelector<DocumentEntryIndexField,DocumentEntry> serviceStopTimeFilterSelector = new ServiceStopTimeFieldFilterSelector("Service Stop Time", valueChangeCallback);
        final IndexFieldFilterSelector<DocumentEntryIndexField,DocumentEntry> authorFilterSelector = new AuthorFieldFilterSelector("Author Person", valueChangeCallback);

        filterSelectors.add(statusFilterSelector);
        filterSelectors.add(entryTypeFilterSelector);
        filterSelectors.add(creationTimeFilterSelector);
        filterSelectors.add(serviceStartTimeFilterSelector);
        filterSelectors.add(serviceStopTimeFilterSelector);
        filterSelectors.add(authorFilterSelector);
    }


    @Override
    public Widget asWidget() {
        return featurePanel;
    }
    public void addToCodeSpec(Map<String, List<String>> codeSpec) {
        codeFilterBank.addToCodeSpec(codeSpec);
    }

    private String displayResult(Result result) {
        StringBuffer buf = new StringBuffer();
        it.assertionsToSb(result, buf);
        clearResult();
        if (!result.passed()) {
            resultPanel.add(new HTML(red(bold("Status: Failed.<br/>",true))));
        } else {
            resultPanel.add(new HTML(bold("Status: Passed.<br/>",true)));
        }
        HTML msgBody = new HTML(buf.toString());
        resultPanel.add(msgBody);
        return buf.toString();
    }

    private void clearResult() {
        resultPanel.clear();
    }

    private void displayResult(Widget widget) {
        clearResult();
        resultPanel.add(widget);
    }

    // skb TODO: on cancel filter, restore last selected item, and restore the normal view.
    /** skb TODO: when filter is applied
          a) show rounded label with an X to remove filter. Place this label next to Contents.
          b) show an edit label, which will restore the filter view
        run advanced mode in single mode
        clear current table selection
     **/
    // skb TODO: when view mode is changed to History, warn user that filter will be cleared.

    // skb TODO: handle show hidden view
    @Override
    public void hideFilter() {
    }


    @Override
    public void setData(List<DocumentEntry> data) {
        initialDeList = data;
        valueChangeCallback.run(new NewSelectedFieldValue(filterSelectors.getFirst(), null, true, false));

    }


    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public void displayFilter() {
        // skb TODO: Clear tree selection because the selected item may not be in the filtered result set.

        String title = "<b>Trial Version Document Entries Filter</b>";
        featurePanel.add(createTitle(HyperlinkFactory.addHTML(title)));
        featurePanel.add(new HTML("<br/>"));
        FlexTable ft = new FlexTable();
        int row=0;
        boolean b = false;

        try {

            // TODO: iterate the selector display components

                    // skb TODO: Count the documents with codes for which we do not have a mapping in our codes.xml
            for (IndexFieldFilterSelector<DocumentEntryIndexField,DocumentEntry> fieldSelectionResult : filterSelectors) {
                featurePanel.add(fieldSelectionResult.asWidget());
            }

        } catch (Exception ex) {
            new PopupMessage(ex.toString());
        } finally {
        }
    }

    @Override
    public boolean applyFilter() {
        return false;
    }

    @Override
    public boolean removeFilter() {
        return false;
    }


    private void popListBox(ListBox listBox, List<String> values) {
        if ((values != null && !values.isEmpty())) {
            if (values == null)
                values = new ArrayList<>();
            for (String value : values) {
                listBox.addItem(value);
            }
        } else {
            listBox.setVisible(false);
        }
    }



}