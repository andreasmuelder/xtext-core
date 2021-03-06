/**
 * Copyright (c) 2016 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtext.ide.server.symbol;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.xtext.findReferences.IReferenceFinder;
import org.eclipse.xtext.findReferences.ReferenceAcceptor;
import org.eclipse.xtext.findReferences.TargetURICollector;
import org.eclipse.xtext.findReferences.TargetURIs;
import org.eclipse.xtext.ide.server.DocumentExtensions;
import org.eclipse.xtext.ide.util.CancelIndicatorProgressMonitor;
import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.EObjectAtOffsetHelper;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.IReferenceDescription;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.IResourceDescriptions;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.service.OperationCanceledManager;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.util.IAcceptor;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

/**
 * @author kosyakov - Initial contribution and API
 * @since 2.11
 */
@Singleton
@SuppressWarnings("all")
public class DocumentSymbolService {
  @Inject
  @Extension
  private DocumentExtensions _documentExtensions;
  
  @Inject
  @Extension
  private EObjectAtOffsetHelper _eObjectAtOffsetHelper;
  
  @Inject
  @Extension
  private IQualifiedNameProvider _iQualifiedNameProvider;
  
  @Inject
  private IReferenceFinder referenceFinder;
  
  @Inject
  private TargetURICollector targetURICollector;
  
  @Inject
  private Provider<TargetURIs> targetURIProvider;
  
  @Inject
  private OperationCanceledManager operationCanceledManager;
  
  @Inject
  private IResourceServiceProvider.Registry resourceServiceProviderRegistry;
  
  public List<? extends Location> getDefinitions(final XtextResource resource, final int offset, final IReferenceFinder.IResourceAccess resourceAccess, final CancelIndicator cancelIndicator) {
    final EObject element = this._eObjectAtOffsetHelper.resolveElementAt(resource, offset);
    if ((element == null)) {
      return CollectionLiterals.<Location>emptyList();
    }
    final ArrayList<Location> locations = CollectionLiterals.<Location>newArrayList();
    final TargetURIs targetURIs = this.collectTargetURIs(element);
    for (final URI targetURI : targetURIs) {
      {
        this.operationCanceledManager.checkCanceled(cancelIndicator);
        final Procedure1<EObject> _function = (EObject obj) -> {
          Location _newLocation = this._documentExtensions.newLocation(obj);
          locations.add(_newLocation);
        };
        this.doRead(resourceAccess, targetURI, _function);
      }
    }
    return locations;
  }
  
  public List<? extends Location> getReferences(final XtextResource resource, final int offset, final IReferenceFinder.IResourceAccess resourceAccess, final IResourceDescriptions indexData, final CancelIndicator cancelIndicator) {
    final EObject element = this._eObjectAtOffsetHelper.resolveElementAt(resource, offset);
    if ((element == null)) {
      return CollectionLiterals.<Location>emptyList();
    }
    final ArrayList<Location> locations = CollectionLiterals.<Location>newArrayList();
    final TargetURIs targetURIs = this.collectTargetURIs(element);
    final IAcceptor<IReferenceDescription> _function = (IReferenceDescription reference) -> {
      final Procedure1<EObject> _function_1 = (EObject obj) -> {
        Location _newLocation = this._documentExtensions.newLocation(obj, reference.getEReference(), reference.getIndexInList());
        locations.add(_newLocation);
      };
      this.doRead(resourceAccess, reference.getSourceEObjectUri(), _function_1);
    };
    ReferenceAcceptor _referenceAcceptor = new ReferenceAcceptor(this.resourceServiceProviderRegistry, _function);
    CancelIndicatorProgressMonitor _cancelIndicatorProgressMonitor = new CancelIndicatorProgressMonitor(cancelIndicator);
    this.referenceFinder.findAllReferences(targetURIs, resourceAccess, indexData, _referenceAcceptor, _cancelIndicatorProgressMonitor);
    return locations;
  }
  
  protected TargetURIs collectTargetURIs(final EObject targetObject) {
    final TargetURIs targetURIs = this.targetURIProvider.get();
    this.targetURICollector.add(targetObject, targetURIs);
    return targetURIs;
  }
  
  public List<? extends SymbolInformation> getSymbols(final XtextResource resource, final CancelIndicator cancelIndicator) {
    final LinkedHashMap<EObject, SymbolInformation> symbols = CollectionLiterals.<EObject, SymbolInformation>newLinkedHashMap();
    final TreeIterator<Object> contents = EcoreUtil.<Object>getAllProperContents(resource, true);
    while (contents.hasNext()) {
      {
        this.operationCanceledManager.checkCanceled(cancelIndicator);
        Object _next = contents.next();
        final EObject obj = ((EObject) _next);
        final SymbolInformation symbol = this.createSymbol(obj);
        if ((symbol != null)) {
          symbols.put(obj, symbol);
          final EObject container = this.getContainer(obj);
          final SymbolInformation containerSymbol = symbols.get(container);
          String _name = null;
          if (containerSymbol!=null) {
            _name=containerSymbol.getName();
          }
          symbol.setContainerName(_name);
        }
      }
    }
    return IterableExtensions.<SymbolInformation>toList(symbols.values());
  }
  
  protected EObject getContainer(final EObject obj) {
    return obj.eContainer();
  }
  
  protected SymbolInformation createSymbol(final EObject object) {
    final String symbolName = this.getSymbolName(object);
    if ((symbolName == null)) {
      return null;
    }
    final SymbolInformation symbol = new SymbolInformation();
    symbol.setName(symbolName);
    symbol.setKind(this.getSymbolKind(object));
    symbol.setLocation(this._documentExtensions.newLocation(object));
    return symbol;
  }
  
  protected String getSymbolName(final EObject object) {
    return this.getSymbolName(this._iQualifiedNameProvider.getFullyQualifiedName(object));
  }
  
  protected SymbolKind getSymbolKind(final EObject object) {
    return this.getSymbolKind(object.eClass());
  }
  
  public List<? extends SymbolInformation> getSymbols(final IResourceDescription resourceDescription, final String query, final IReferenceFinder.IResourceAccess resourceAccess, final CancelIndicator cancelIndicator) {
    final LinkedList<SymbolInformation> symbols = CollectionLiterals.<SymbolInformation>newLinkedList();
    Iterable<IEObjectDescription> _exportedObjects = resourceDescription.getExportedObjects();
    for (final IEObjectDescription description : _exportedObjects) {
      {
        this.operationCanceledManager.checkCanceled(cancelIndicator);
        boolean _filter = this.filter(description, query);
        if (_filter) {
          final SymbolInformation symbol = this.createSymbol(description);
          if ((symbol != null)) {
            symbols.add(symbol);
            final Procedure1<EObject> _function = (EObject obj) -> {
              symbol.setLocation(this._documentExtensions.newLocation(obj));
            };
            this.doRead(resourceAccess, description.getEObjectURI(), _function);
          }
        }
      }
    }
    return symbols;
  }
  
  protected boolean filter(final IEObjectDescription description, final String query) {
    return description.getQualifiedName().toLowerCase().toString().contains(query.toLowerCase());
  }
  
  protected SymbolInformation createSymbol(final IEObjectDescription description) {
    final String symbolName = this.getSymbolName(description);
    if ((symbolName == null)) {
      return null;
    }
    final SymbolInformation symbol = new SymbolInformation();
    symbol.setName(symbolName);
    symbol.setKind(this.getSymbolKind(description));
    return symbol;
  }
  
  protected String getSymbolName(final IEObjectDescription description) {
    return this.getSymbolName(description.getQualifiedName());
  }
  
  protected SymbolKind getSymbolKind(final IEObjectDescription description) {
    return this.getSymbolKind(description.getEClass());
  }
  
  protected String getSymbolName(final QualifiedName qualifiedName) {
    String _string = null;
    if (qualifiedName!=null) {
      _string=qualifiedName.toString();
    }
    return _string;
  }
  
  protected SymbolKind getSymbolKind(final EClass type) {
    return SymbolKind.Property;
  }
  
  protected void doRead(final IReferenceFinder.IResourceAccess resourceAccess, final URI objectURI, final Procedure1<? super EObject> acceptor) {
    final IUnitOfWork<Object, ResourceSet> _function = (ResourceSet resourceSet) -> {
      final EObject object = resourceSet.getEObject(objectURI, true);
      if ((object != null)) {
        acceptor.apply(object);
      }
      return null;
    };
    resourceAccess.<Object>readOnly(objectURI, _function);
  }
}
