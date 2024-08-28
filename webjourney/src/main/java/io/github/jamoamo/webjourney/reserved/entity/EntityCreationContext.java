/*
 * The MIT License
 *
 * Copyright 2024 James Amoore.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.github.jamoamo.webjourney.reserved.entity;

import java.util.Stack;

/**
 * The context of the current entity creation context.
 * Gives indication of current location in entity tree the entity creation process is.
 *
 * @author James Amoore
 */
public final class EntityCreationContext
{
	 private class EntityBreadcrumb
	 {
		  private final String fieldName;
		  private final Integer collectionIndex;

		  EntityBreadcrumb(String fieldName)
		  {
				this.fieldName = fieldName;
				this.collectionIndex = null;
		  }

		  EntityBreadcrumb(String fieldName, int collectionIndex)
		  {
				this.fieldName = fieldName;
				this.collectionIndex = collectionIndex;
		  }

		  @Override
		  public String toString()
		  {
				return this.fieldName + (this.collectionIndex != null ? "[" + this.collectionIndex + "]" : "");
		  }

	 }

	 private EntityDefn baseEntity;
	 private Stack<EntityBreadcrumb> entityBreadCrumbs;
	 private Stack<Integer> collectionIndices;

	 EntityCreationContext(EntityDefn entityDefn)
	 {
		  this.baseEntity = entityDefn;
		  this.entityBreadCrumbs = new Stack<>();
		  this.collectionIndices = new Stack<>();
	 }
	 
	 protected void processField(EntityFieldDefn entityFieldDefn)
	 {
		  this.entityBreadCrumbs.push(new EntityBreadcrumb(entityFieldDefn.getFieldName()));
	 }
	 
	 /**
	  * Starts the processing of a collection.
	  */
	 public void startCollection()
	 {
		  this.collectionIndices.push(-1);
	 }

	 /**
	  * Process a new collection item.
	  */
	 public void processCollectionItem()
	 {
		  if(this.entityBreadCrumbs.empty())
		  {
				return;
		  }
		  EntityBreadcrumb existingItem = this.entityBreadCrumbs.pop();
		  EntityBreadcrumb newItem =
				new EntityBreadcrumb(existingItem.fieldName, existingItem.collectionIndex == null ? 0 :
					 existingItem.collectionIndex + 1);
		  this.entityBreadCrumbs.push(newItem);
		  
		  int index = this.collectionIndices.pop();
		  this.collectionIndices.push(++index);
	 }
	 
	 /**
	  * Ends the existing collection processing.
	  */
	 public void endCollection()
	 {
		  this.collectionIndices.pop();
	 }
	 
	 /**
	  * Gets the index of the existing collection item.
	  * @return the existing index.
	  */
	 public Integer getExistingIndex()
	 {
		 if(this.collectionIndices.empty())
		 {
			 return null;
		 }
		 return this.collectionIndices.peek();
	 }

	 protected void fieldProcessComplete()
	 {
		  this.entityBreadCrumbs.pop();
	 }

	 /**
	  * Get the current context representation.
	  * 
	  * @return the context representation
	  */
	 public String getContext()
	 {
		  StringBuilder contextBuilder = new StringBuilder(this.baseEntity.getFieldType()
				.getCanonicalName());
		  for(EntityBreadcrumb breadcrumb : this.entityBreadCrumbs)
		  {
				contextBuilder.append("->");
				contextBuilder.append(breadcrumb.toString());
		  }
		  return contextBuilder.toString();
	 }

}
