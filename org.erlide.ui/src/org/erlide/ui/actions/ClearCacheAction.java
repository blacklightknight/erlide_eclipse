/**
 *
 */
package org.erlide.ui.actions;

import java.io.File;
import java.util.ResourceBundle;

import org.eclipse.ui.texteditor.TextEditorAction;
import org.erlide.core.ErlangPlugin;
import org.erlide.core.erlang.ErlScanner;
import org.erlide.core.erlang.IErlModule;
import org.erlide.ui.editors.erl.ErlangEditor;

/**
 * @author jakob
 * 
 */
public class ClearCacheAction extends TextEditorAction {

	private final ErlangEditor erlangEditor;
	private final String NOPARSE_CACHE_SUFFIX = ".noparse";
	private final String SCANNER_CACHE_SUFFIX = ".scan";
	private String suffixes[] = {NOPARSE_CACHE_SUFFIX, SCANNER_CACHE_SUFFIX};

	public ClearCacheAction(final ResourceBundle bundle, final String prefix,
			final ErlangEditor erlangEditor) {
		super(bundle, prefix, erlangEditor);
		this.erlangEditor = erlangEditor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		final IErlModule module = erlangEditor.getModule();
		for (String suffix : suffixes) {
			final String cacheFileOSPath = ErlangPlugin.getDefault()
			.getStateLocation().append(
					ErlScanner.createScannerModuleName(module)
					+ suffix).toOSString();
			final File cacheFile = new File(cacheFileOSPath);
			cacheFile.delete();
		}
		erlangEditor.resetParser();
	}
}
