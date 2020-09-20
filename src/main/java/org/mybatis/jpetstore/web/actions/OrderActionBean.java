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
package org.mybatis.jpetstore.web.actions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;


import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.SessionScope;
import net.sourceforge.stripes.integration.spring.SpringBean;


import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.mybatis.jpetstore.domain.Order;
import org.mybatis.jpetstore.service.OrderService;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;


/**
 * The Class OrderActionBean.
 *
 * @author Eduardo Macarron
 */
@SessionScope
public class OrderActionBean extends AbstractActionBean {

	  private static final long serialVersionUID = -6171288227470176272L;

	

	  private static final List<String> CARD_TYPE_LIST;

	  @SpringBean
	  private transient OrderService orderService;

	  private Order order = new Order();
	  
	  private boolean shippingAddressRequired;
	  private boolean confirmed;
	  private List<Order> orderList;

	  static {
	    CARD_TYPE_LIST = Collections.unmodifiableList(Arrays.asList("Visa", "MasterCard", "American Express"));
	  }

	  public int getOrderId() {
	    return order.getOrderId();
	  }

	  public void setOrderId(int orderId) {
	    order.setOrderId(orderId);
	  }

	  public Order getOrder() {
	    return order;
	  }

	  public void setOrder(Order order) {
	    this.order = order;
	  }

	  public boolean isShippingAddressRequired() {
	    return shippingAddressRequired;
	  }

	  public void setShippingAddressRequired(boolean shippingAddressRequired) {
	    this.shippingAddressRequired = shippingAddressRequired;
	  }

	  public boolean isConfirmed() {
	    return confirmed;
	  }

	  public void setConfirmed(boolean confirmed) {
	    this.confirmed = confirmed;
	  }

	  public List<String> getCreditCardTypes() {
	    return CARD_TYPE_LIST;
	  }

	  public List<Order> getOrderList() {
	    return orderList;
	  }

	  /**
	   * List orders.
	   *
	   * @return the resolution
	 * @throws ParseException 
	   */
	  String user;
	  public Resolution listOrders() throws ParseException {
		  user = null;
		    HttpServletRequest request = context.getRequest();
		    String userid = request.getParameter("id");   
		    String url="http://account:8081/jpetstore/actions/Account.action?getUserinfo&id="+userid;
	    	RestTemplate restTemplate = new RestTemplate();
	    	String resp = restTemplate.getForObject(url, String.class);
	    	JSONParser parser = new JSONParser();
	    	JSONObject id = (JSONObject)parser.parse(resp);
		    
	    	user = (String)id.get("username");
		    
		    
	    	
		    orderList = orderService.getOrdersByUsername(user);
		  return new Resolution()
			{

				@Override
				public void execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
					// TODO Auto-generated method stub
					
					
								
					response.setCharacterEncoding("utf-8");
			        response.setContentType("application/json");

					Gson gson = new Gson();
					
					String jsonlist = gson.toJson(orderList);
					PrintWriter out = response.getWriter();
					out.write(jsonlist);
			        out.flush();
			        out.close();
			        orderList = null;

				}

			};
		  
	  }
	  
	  
	  
	  
	  


	  /**
	   * New order form.
	   *
	   * @return the resolution
	 * @throws ParseException 
	   */
  


	  /**
	   * New order.
	   *
	   * @return the resolution
	 * @throws IOException 
	   */
	  public Resolution newOrder() throws IOException {
		  
		  HttpServletRequest request = context.getRequest();
		  String body = null;
	        StringBuilder stringBuilder = new StringBuilder();
	        BufferedReader bufferedReader = null;
	 
	      
	            InputStream inputStream = request.getInputStream();
	            if (inputStream != null) {
	                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
	                char[] charBuffer = new char[128];
	                int bytesRead = -1;
	                while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
	                    stringBuilder.append(charBuffer, 0, bytesRead);
	                }
	            }
	       

	        body = stringBuilder.toString();
	        Gson gson = new Gson();
			order = gson.fromJson(body, Order.class);

	      orderService.insertOrder(order);
	      
	      
	      
	      return new Resolution()
	  	{

	  		@Override
	  		public void execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
	  			
	  			
	  			// TODO Auto-generated method stub
	  			
	  			response.setCharacterEncoding("utf-8");
	  	        response.setContentType("application/json");

	  			Gson gson = new Gson();
	  			Map<String, String> json_result= new HashMap();
	  			json_result.put("result", "success");
	  			
	  			String jsonlist = gson.toJson(json_result);
	  			PrintWriter out = response.getWriter();
	  			out.write(jsonlist);
	  	        out.flush();
	  	        out.close();
	  			
	  		}

	  	};
	      
	     

	  }

	  /**
	   * View order.
	   *
	   * @return the resolution
	 * @throws ParseException 
	   */

	  public Resolution viewOrder() throws ParseException {
		  
		  return new Resolution()
			{

				@Override
				public void execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
					// TODO Auto-generated method stub
					String getid = request.getParameter("id");
					
					int id = Integer.parseInt(getid);
					response.setCharacterEncoding("utf-8");
		  	        response.setContentType("application/json");

		  			Gson gson = new Gson();

					order = orderService.getOrder(id);
					String jsonlist = gson.toJson(order);
		  			PrintWriter out = response.getWriter();
		  			out.write(jsonlist);
		  	        out.flush();
		  	        out.close();
			       
				}

			};
		  
		  
	 
	    
	
	  }
	  


	  /**
	   * Clear.
	   */
	  public void clear() {
		    order = new Order();
		    shippingAddressRequired = false;
		    confirmed = false;
		    orderList = null;
		  }
}

