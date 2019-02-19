/*
 * Copyright (c) 2011-2015 Spotify AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.spotify.asyncdatastoreclient;

import com.google.api.client.util.Lists;
import com.google.datastore.v1.Projection;
import com.google.datastore.v1.PropertyReference;
import com.google.protobuf.ByteString;
import com.google.protobuf.Int32Value;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A query statement.
 *
 * Retrieves one or more entities that satisfy a given criteria and order.
 */
public class Query implements Statement {

  private final com.google.datastore.v1.Query.Builder query;
  private final List<Filter> filters;

  Query() {
    query = com.google.datastore.v1.Query.newBuilder();
    filters = Lists.newArrayList();
  }

  /**
   * Specifies that only entity keys should be returned and not all
   * properties.
   *
   * @return this query statement.
   */
  public Query keysOnly() {
    query.addProjection(
        Projection
            .newBuilder()
            .setProperty(PropertyReference.newBuilder().setName("__key__").build()));
    return this;
  }

  /**
   * Only return a given set of properties, otherwise known as a Projection Query.
   *
   * @param properties one or more property names to return.
   * @return this query statement.
   */
  public Query properties(final String... properties) {
    return properties(Arrays.asList(properties));
  }

  /**
   * Only return a given set of properties, otherwise known as a Projection Query.
   *
   * @param properties one or more property names to return.
   * @return this query statement.
   */
  public Query properties(final List<String> properties) {
    query.addAllProjection(properties.stream()
                               .map(property -> Projection.newBuilder()
                                   .setProperty(com.google.datastore.v1.PropertyReference
                                     .newBuilder()
                                     .setName(property))
                                  .build())
                               .collect(Collectors.toList()));
    return this;
  }

  /**
   * Query entities of a given kind.
   *
   * @param kind the kind of entity to query.
   * @return this query statement.
   */
  public Query kindOf(final String kind) {
    query.addKind(com.google.datastore.v1.KindExpression.newBuilder().setName(kind));
    return this;
  }

  /**
   * Apply a given filter to the query.
   *
   * @param filter the query filter to apply.
   * @return this query statement.
   */
  public Query filterBy(final Filter filter) {
    filters.add(filter);
    return this;
  }

  /**
   * Apply a given order to the query.
   *
   * @param order the query order to apply.
   * @return this query statement.
   */
  public Query orderBy(final Order order) {
    query.addOrder(order.getPb());
    return this;
  }

  /**
   * Apply a given group to the query.
   *
   * @param group the query group to apply.
   * @return this query statement.
   */
  public Query groupBy(final Group group) {
    query.addDistinctOn(group.getPb());
    return this;
  }

  /**
   * Tell Datastore to begin returning entities from a given cursor
   * position. This is used to page results; the last cursor position
   * is returned in {@code QueryResult}.
   *
   * @param cursor the last query cursor position.
   * @return this query statement.
   */
  public Query fromCursor(final ByteString cursor) {
    query.setStartCursor(cursor);
    return this;
  }

  /**
   * Limit the number of entities returned in this query. The last
   * cursor position will be returned in {@code QueryResult} if more
   * entities are required.
   *
   * @param limit the maximum number of entities to return.
   * @return this query statement.
   */
  public Query limit(final int limit) {
    query.setLimit(Int32Value.newBuilder().setValue(limit));
    return this;
  }

  /**
   * Tell Datastore to skip given number of entities.
   *
   * @param offset the number of entities to be skipped.
   * @return this query statement.
   */
  public Query offset(final int offset) {
    query.setOffset(offset);
    return this;
  }

  /**
   * remove existing offset.
   * @return this query statement.
   */
  public Query clearOffset(){
    query.clearOffset();
    return this;
  }

  public int getOffset(){
    return query.getOffset();
  }

  com.google.datastore.v1.Query getPb(String namespace) {
    if (filters.size() == 1) {
      query.setFilter(filters.get(0).getPb(namespace));
    } else if (filters.size() > 1) {
      query.setFilter(com.google.datastore.v1.Filter.newBuilder()
                          .setCompositeFilter(
                              com.google.datastore.v1.CompositeFilter.newBuilder()
                                  .addAllFilters(filters.stream().map(f -> f.getPb(namespace)).collect(Collectors.toList()))
                                  .setOp(com.google.datastore.v1.CompositeFilter.Operator.AND)));
    }
    return query.build();
  }
}
