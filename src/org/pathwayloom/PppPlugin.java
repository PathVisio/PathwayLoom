// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2009 BiGCaT Bioinformatics
//
// Licensed under the Apache License, Version 2.0 (the "License"); 
// you may not use this file except in compliance with the License. 
// You may obtain a copy of the License at 
// 
// http://www.apache.org/licenses/LICENSE-2.0 
//  
// Unless required by applicable law or agreed to in writing, software 
// distributed under the License is distributed on an "AS IS" BASIS, 
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
// See the License for the specific language governing permissions and 
// limitations under the License.
//
package org.pathwayloom;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;
import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.JDOMException;
import org.pathvisio.core.data.GdbManager;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.model.ConverterException;
import org.pathvisio.core.model.DataNodeType;
import org.pathvisio.core.model.ObjectType;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.util.ProgressKeeper;
import org.pathvisio.core.view.GeneProduct;
import org.pathvisio.core.view.Graphics;
import org.pathvisio.core.view.VPathwayElement;
import org.pathvisio.desktop.PvDesktop;
import org.pathvisio.desktop.plugin.Plugin;
import org.pathvisio.gui.PathwayElementMenuListener.PathwayElementMenuHook;
import org.pathvisio.gui.ProgressDialog;
import org.pathwayloom.chembl.ChEMBLCompoundSpaqrlPlugin;
import org.pathwayloom.chembl.ChEMBLProteinSparqlPlugin;
import org.pathwayloom.uniprot.UniprotEnzymeProteinSparqlPlugin;
import org.pathwayloom.uniprot.UniprotEnzymeSparqlPlugin;
import org.pathwayloom.uniprot.UniprotProteinSparqlPlugin;
import org.pathwayloom.utils.SparqlQueryParser;
import org.pathwayloom.wikidata.WikidataCompoundSparqlPlugin;
import org.pathwayloom.wikidata.WikidataEncodedBySparqlPlugin;
import org.pathwayloom.wikidata.WikidataEncodesSparqlPlugin;
import org.pathwayloom.wikidata.WikidataSparqlPlugin;
import org.pathwayloom.wpsparql.WikiPathwaysSparqlPluginAdvanced;
import org.pathwayloom.wpsparql.WikiPathwaysSparqlPluginBasic;
import org.xml.sax.SAXException;

/**
 * PathwayLoom plugin - 
 * adds a sidebar where other plug-ins can add suggestions.
 * Also hooks into the right-click menu
 */
public class PppPlugin implements Plugin, PathwayElementMenuHook {
	
	public static final double DATANODE_MWIDTH = 60;
	public static final double DATANODE_MHEIGHT = 20;

	private PvDesktop desktop;
	private PppPane pane;
	private SuggestionAction sparqlLoomWpSparqlBasic;
	private SuggestionAction sparqlLoomWpSparqlAdvanced;
	private SuggestionAction sparqlLoomWdSparqlBasic;
	private SuggestionAction sparqlLoomWdSparqlEncodedby;
	private SuggestionAction sparqlLoomWdSparqlEncodes;
	private SuggestionAction sparqlLoomWdSparqlCompound;
	private SuggestionAction sparqlLoomUpSparqlProtein;
	private SuggestionAction sparqlLoomUpSparqlEnzyme;
	private SuggestionAction sparqlLoomUpSparqlEnzymeProtein;
	private SuggestionAction sparqlLoomChSparqlProtein;
	private SuggestionAction sparqlLoomChSparqlCompound;
	
	/**
	 * return the existing PppPane
	 */
	public PppPane getPane() {
		return pane;
	}

	/**
	 * Initialize plug-in, is called by plugin manager.
	 */
	public void init(PvDesktop desktop)  {
		this.desktop = desktop;
		
		//Parse github queries
		try {
			SparqlQueryParser.init();
		} catch (ParserConfigurationException | SAXException | IOException
				| JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// create new PppPane and add it to the side bar.
		pane = new PppPane(desktop);
		JTabbedPane sidebarTabbedPane = desktop.getSideBarTabbedPane();
		sidebarTabbedPane.add(PppPane.TITLE, pane);
		
		// register our pathwayElementMenu hook.
		desktop.addPathwayElementMenuHook(this);
		
		// register right click actions
		GdbManager gdbManager = desktop.getSwingEngine().getGdbManager();
		
		WikiPathwaysSparqlPluginBasic wpb = new WikiPathwaysSparqlPluginBasic(gdbManager);
		sparqlLoomWpSparqlBasic = new SuggestionAction(this, "Only binary interactions",wpb);
		
		WikiPathwaysSparqlPluginAdvanced wpa = new WikiPathwaysSparqlPluginAdvanced(gdbManager);
		sparqlLoomWpSparqlAdvanced = new SuggestionAction(this, "All interactions", wpa);		
		
		WikidataSparqlPlugin wd  = new WikidataSparqlPlugin(gdbManager);
		sparqlLoomWdSparqlBasic = new SuggestionAction(this, wd.getMenu_label(), wd);
		
		WikidataEncodedBySparqlPlugin web = new WikidataEncodedBySparqlPlugin(gdbManager);
		sparqlLoomWdSparqlEncodedby = new SuggestionAction(this, web.getMenu_label(), web);
		
		WikidataEncodesSparqlPlugin we = new WikidataEncodesSparqlPlugin(gdbManager);		
		sparqlLoomWdSparqlEncodes = new SuggestionAction(this, we.getMenu_label(), we);
		
		WikidataCompoundSparqlPlugin wc = new WikidataCompoundSparqlPlugin(gdbManager);		
		sparqlLoomWdSparqlCompound = new SuggestionAction(this, wc.getMenu_label(), wc);
		
		UniprotProteinSparqlPlugin up = new UniprotProteinSparqlPlugin(gdbManager);
		sparqlLoomUpSparqlProtein = new SuggestionAction(this, up.getMenu_label(), up);
		
		UniprotEnzymeSparqlPlugin ue = new UniprotEnzymeSparqlPlugin(gdbManager);
		sparqlLoomUpSparqlEnzyme = new SuggestionAction(this, ue.getMenu_label(), ue);
		
		UniprotEnzymeProteinSparqlPlugin uep = new UniprotEnzymeProteinSparqlPlugin(gdbManager);
		sparqlLoomUpSparqlEnzymeProtein = new SuggestionAction(this, uep.getMenu_label(), uep);
		
		ChEMBLProteinSparqlPlugin cp = new ChEMBLProteinSparqlPlugin(gdbManager);
		sparqlLoomChSparqlProtein = new SuggestionAction(this, cp.getMenu_label(), cp);
		
		ChEMBLCompoundSpaqrlPlugin cc = new ChEMBLCompoundSpaqrlPlugin(gdbManager);
		sparqlLoomChSparqlCompound = new SuggestionAction(this, cc.getMenu_label(), cc);
	}

	public void done() {
		// removes side pane
		desktop.getSideBarTabbedPane().remove(pane);
		// removes right click menu
		desktop.getSwingEngine().getApplicationPanel().getPathwayElementMenuListener().removePathwayElementMenuHook(this);
	}

	/**
	 * Action to be added to right-click menu.
	 * This action is recycled, i.e. it's instantiated only once but
	 * added each time to the new popup Menu.
	 */
	private class SuggestionAction extends AbstractAction {
		private PppPlugin parent;
		private String name;

		private GeneProduct elt;
		private Suggestion suggestion;

		/**
		 * set the element that will be used as input for the suggestion.
		 * Call this before adding to the menu. 
		 */
		void setElement (GeneProduct anElt) {
			elt = anElt;
			setEnabled(suggestion.canSuggest(elt.getPathwayElement()));
		}

		

		/** called when plug-in is initialized */
		SuggestionAction(PppPlugin aParent, String name, Suggestion suggestion) {
			parent = aParent;
			this.suggestion = suggestion;
			this.name = name;
			putValue(NAME, name );
		}

		/** called when user clicks on the menu item */



		public void actionPerformed(ActionEvent e) {
			final PppPane pane = parent.getPane();
			//pane.removeAll();
			//pane.validate();
			final ProgressKeeper pk = new ProgressKeeper();
			final ProgressDialog pd = new ProgressDialog(desktop.getFrame(), "Querying " +  name, pk, true, true);
			pk.setTaskName("Running query");

			SwingWorker<PathwayBuilder, Void> worker = new SwingWorker<PathwayBuilder, Void>(){

				@Override
				protected PathwayBuilder doInBackground() throws Exception {
					PathwayBuilder result = suggestion.doSuggestion(elt.getPathwayElement());
					return result;
				}

				@Override
				protected void done() {
					if (pk.isCancelled()) return; // don't add if user pressed cancel.
					try {
						pane.addPart("Pathway Loom: " + name, get());
					} catch (InterruptedException e) {
						Logger.log.error("Operation interrupted", e);
						JOptionPane.showMessageDialog(
								pane, "Operation interrupted",
								"Error", JOptionPane.ERROR_MESSAGE
						);
					} catch (ExecutionException e) {
						// exception generated by the suggestion implementation itself
						Throwable cause = e.getCause();
						cause.printStackTrace();
						Logger.log.error("Unable to get suggestions", cause);
						JOptionPane.showMessageDialog(
								pane, "Unable to get suggestions: " + cause.getMessage(),
								"Error", JOptionPane.ERROR_MESSAGE
						);
					}
					pk.finished();
				}
			};
			worker.execute();
			pd.setVisible(true);
		}
	}

	/**
	 * callback, is called when user clicked with RMB on a pathway element.
	 * @throws IOException 
	 * @throws ConverterException 
	 */
	public void pathwayElementMenuHook(final VPathwayElement e, JPopupMenu menu) {
		// only show right click menu if element is GeneProduct and 
		// element is annotated (Xref)
		if (e instanceof Graphics) {
			Graphics g = (Graphics) e;
			PathwayElement elem = g.getPathwayElement();
			if (elem.getObjectType().equals(ObjectType.DATANODE)
					&& (DataNodeType.byName(elem.getDataNodeType()).equals(DataNodeType.GENEPRODUCT)
					|| DataNodeType.byName(elem.getDataNodeType()).equals(DataNodeType.METABOLITE)
					|| DataNodeType.byName(elem.getDataNodeType()).equals(DataNodeType.PROTEIN))
					) {
				if (elem.getXref().getId() != null && elem.getXref().getDataSource() != null) {
					
					JMenu submenu = new JMenu("Pathway Loom");
					JMenuItem titleMenu = submenu.add("Pathway Loom BETA");
					titleMenu.setEnabled(false);
					titleMenu.setFont(new Font("sansserif", Font.BOLD, 16));
					titleMenu.setBackground(Color.white);
					titleMenu.setForeground(Color.gray);
					
					JMenu wpSubmenu = new JMenu("WikiPathways");
					JMenu upSubmenu = new JMenu("Uniprot");
					JMenu wdSubmenu = new JMenu("Wikidata");
					JMenu chSubmenu = new JMenu("ChEMBL");
					
					sparqlLoomWpSparqlBasic.setElement((GeneProduct) e);
					sparqlLoomWpSparqlAdvanced.setElement((GeneProduct) e);					
					sparqlLoomWdSparqlBasic.setElement((GeneProduct) e);
					sparqlLoomWdSparqlEncodedby.setElement((GeneProduct) e);
					sparqlLoomWdSparqlEncodes.setElement((GeneProduct) e);
					sparqlLoomWdSparqlCompound.setElement((GeneProduct) e);
					sparqlLoomUpSparqlProtein.setElement((GeneProduct) e);
					sparqlLoomUpSparqlEnzyme.setElement((GeneProduct) e);
					sparqlLoomUpSparqlEnzymeProtein.setElement((GeneProduct) e);
					sparqlLoomChSparqlProtein.setElement((GeneProduct) e);
					sparqlLoomChSparqlCompound.setElement((GeneProduct) e);
					
					wpSubmenu.add(sparqlLoomWpSparqlBasic);
					wpSubmenu.add(sparqlLoomWpSparqlAdvanced);
					submenu.add(wpSubmenu);
					
					wdSubmenu.add(sparqlLoomWdSparqlBasic);
					wdSubmenu.add(sparqlLoomWdSparqlEncodedby);
					wdSubmenu.add(sparqlLoomWdSparqlEncodes);
					wdSubmenu.add(sparqlLoomWdSparqlCompound);
					submenu.add(wdSubmenu);
					
					upSubmenu.add(sparqlLoomUpSparqlProtein);
					upSubmenu.add(sparqlLoomUpSparqlEnzyme);
					upSubmenu.add(sparqlLoomUpSparqlEnzymeProtein);
					submenu.add(upSubmenu);
					
					chSubmenu.add(sparqlLoomChSparqlProtein);
					chSubmenu.add(sparqlLoomChSparqlCompound);
					submenu.add(chSubmenu);
					
					menu.add(submenu);
					
				}
			}
		}
	}
}

