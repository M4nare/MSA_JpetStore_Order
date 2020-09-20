/**
 *    Copyright 2010-2018 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.jpetstore.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.mybatis.jpetstore.domain.Item;
import org.mybatis.jpetstore.domain.Order;
import org.mybatis.jpetstore.domain.Product;
import org.mybatis.jpetstore.domain.Sequence;
import org.mybatis.jpetstore.mapper.LineItemMapper;
import org.mybatis.jpetstore.mapper.OrderMapper;
import org.mybatis.jpetstore.mapper.SequenceMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

/**
 * The Class OrderService.
 *
 * @author Eduardo Macarron
 */
@Service
public class OrderService {

 
  private final OrderMapper orderMapper;
  private final SequenceMapper sequenceMapper;
  private final LineItemMapper lineItemMapper;

  public OrderService(OrderMapper orderMapper, SequenceMapper sequenceMapper,
      LineItemMapper lineItemMapper) {
    this.orderMapper = orderMapper;
    this.sequenceMapper = sequenceMapper;
    this.lineItemMapper = lineItemMapper;
  }

  /**
   * Insert order.
   *
   * @param order
   *          the order
   */
  @Transactional
  public void insertOrder(Order order) {
    order.setOrderId(getNextId("ordernum"));
    order.getLineItems().forEach(lineItem -> {
      String itemId = lineItem.getItemId();
      Integer increment = lineItem.getQuantity();
     
      
      
      
    // itemMapper.updateInventoryQuantity(param);
      
      
      String url="http://catalog:8080/jpetstore/actions/Catalog.action?updateInventoryQuantity&itemId="+itemId+"&increment="+increment;
     	RestTemplate restTemplate = new RestTemplate();
     	String resp = restTemplate.getForObject(url, String.class);
     	JSONParser parser = new JSONParser();
    	JSONObject result = null;
    	try {
			result = (JSONObject)parser.parse(resp);
			
			System.out.println((String)result.get("result"));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

     
     //post
    });

    orderMapper.insertOrder(order);
    orderMapper.insertOrderStatus(order);
    order.getLineItems().forEach(lineItem -> {
    	
    	System.out.println(order.getLineItems());
      lineItem.setOrderId(order.getOrderId());
      lineItemMapper.insertLineItem(lineItem);
    });
  }

  /**
   * Gets the order.
   *
   * @param orderId
   *          the order id
   * @return the order
   * 
   * @throws ParseException 
   */
  
  @Transactional
  public Order getOrder(int orderId) {
    Order order = orderMapper.getOrder(orderId);
    order.setLineItems(lineItemMapper.getLineItemsByOrderId(orderId));

    order.getLineItems().forEach(lineItem -> {
    	
    	// Item mapper 대신 사용 하는 REST API
    	String url="http://catalog:8080/jpetstore/actions/Catalog.action?getitem&item="+lineItem.getItemId();
  	    String url2="http://catalog:8080/jpetstore/actions/Catalog.action?getProductid=&item="+lineItem.getItemId();
    	RestTemplate restTemplate = new RestTemplate();
    	String resp = restTemplate.getForObject(url, String.class);
    	String resp2 = restTemplate.getForObject(url2, String.class);
    	JSONParser parser = new JSONParser();
    	JSONObject getitem = null;
		try {
			getitem = (JSONObject)parser.parse(resp);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	JSONObject getproduct = null;
		try {
			getproduct = (JSONObject)parser.parse(resp2);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	//Product info
    	Item item1 = new Item();
    	Product product1 = new Product();

    	product1.setProductId((String)getproduct.get("productId"));
    	product1.setCategoryId((String)getproduct.get("categoryId"));
    	product1.setName((String)getproduct.get("name"));
    	product1.setDescription((String)getproduct.get("description"));
    	item1.setItemId((String)getitem.get("itemId"));
    	
    	Double a = (Double)getitem.get("listPrice");
    	BigDecimal price = new BigDecimal(a);
    	item1.setListPrice(price);
    	
    	
    	Double b = (Double)getitem.get("unitCost");
    	BigDecimal cost = new BigDecimal(b);
    	item1.setUnitCost(cost);
    	
    	
    	int SupplierId = Integer.parseInt(String.valueOf(getitem.get("supplierId")));
    	item1.setSupplierId(SupplierId);
    	

    	item1.setStatus((String)getitem.get("status"));
    	item1.setAttribute1((String)getitem.get("attribute1"));
    	item1.setProduct(product1);
    	int Quantity = Integer.parseInt(String.valueOf(getitem.get("quantity")));
    	
    	item1.setQuantity(Quantity);
    	
      lineItem.setItem(item1);
    });

    return order;
  }

  /**
   * Gets the orders by username.
   *
   * @param username
   *          the username
   * @return the orders by username
   */
  public List<Order> getOrdersByUsername(String username) {
    return orderMapper.getOrdersByUsername(username);
  }

  /**
   * Gets the next id.
   *
   * @param name
   *          the name
   * @return the next id
   */
  public int getNextId(String name) {
    Sequence sequence = sequenceMapper.getSequence(new Sequence(name, -1));
    if (sequence == null) {
      throw new RuntimeException(
          "Error: A null sequence was returned from the database (could not get next " + name + " sequence).");
    }
    Sequence parameterObject = new Sequence(name, sequence.getNextId() + 1);
    sequenceMapper.updateSequence(parameterObject);
    return sequence.getNextId();
  }

}
