/*******************************************************************************
 * Copyright (c) 2004 Vlad Dumitrescu and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vlad Dumitrescu
 *******************************************************************************/
package org.erlide.ui.actions;

import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.erlide.core.ErlangPlugin;
import org.erlide.core.erlang.ErlangCore;
import org.erlide.core.preferences.OldErlangProjectProperties;
import org.erlide.runtime.backend.ErlideBackend;

public class ToggleNatureAction implements IObjectActionDelegate {

	private ISelection fSelection;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(final IAction action) {
		if (fSelection instanceof IStructuredSelection) {
			for (final Iterator<?> it = ((IStructuredSelection) fSelection)
					.iterator(); it.hasNext();) {
				final Object element = it.next();
				IProject project = null;
				if (element instanceof IProject) {
					project = (IProject) element;
				} else if (element instanceof IAdaptable) {
					project = (IProject) ((IAdaptable) element)
							.getAdapter(IProject.class);
				}
				if (project != null) {
					toggleNature(project);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action
	 * .IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(final IAction action,
			final ISelection selection) {
		fSelection = selection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.
	 * action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart(final IAction action,
			final IWorkbenchPart targetPart) {
	}

	/**
	 * Toggles sample nature on a project
	 * 
	 * @param project
	 *            to have sample nature added or removed
	 */
	private void toggleNature(final IProject project) {
		try {
			final IProjectDescription description = project.getDescription();
			final String[] natures = description.getNatureIds();
			final OldErlangProjectProperties prefs = ErlangCore
					.getProjectProperties(project);

			for (int i = 0; i < natures.length; ++i) {
				if (ErlangPlugin.NATURE_ID.equals(natures[i])) {
					// Remove the nature
					final String[] newNatures = new String[natures.length - 1];
					System.arraycopy(natures, 0, newNatures, 0, i);
					System.arraycopy(natures, i + 1, newNatures, i,
							natures.length - i - 1);
					description.setNatureIds(newNatures);
					project.setDescription(description, null);

					// remove code path to backend
					final String out = project.getLocation().append(
							prefs.getOutputDir()).toString();
					for (final ErlideBackend b : ErlangCore.getBackendManager()
							.getExecutionBackends(project)) {
						b.removePath(prefs.getUsePathZ(), out);
					}
					return;
				}
			}

			// Add the nature, it will be put first
			final String[] newNatures = new String[natures.length + 1];
			System.arraycopy(natures, 0, newNatures, 1, natures.length);
			newNatures[0] = ErlangPlugin.NATURE_ID;
			description.setNatureIds(newNatures);
			project.setDescription(description, null);

			// add code path to backend
			final String out = project.getLocation().append(
					prefs.getOutputDir()).toString();
			for (final ErlideBackend b : ErlangCore.getBackendManager()
					.getExecutionBackends(project)) {
				b.addPath(prefs.getUsePathZ(), out);
			}

		} catch (final CoreException e) {
		}
	}
}
